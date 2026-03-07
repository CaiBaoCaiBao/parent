package users.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import common.common.AccessPayload;
import common.common.RefreshPayload;
import common.dict.DictConstants;
import common.enums.RedisKey;
import common.enums.ResultCode;
import common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import users.client.NotificationClient;
import users.mapper.UserProfileMapper;
import users.mapper.UsersMapper;
import users.pojo.dto.ForgotPasswordDTO;
import users.pojo.dto.LoginDTO;
import users.pojo.dto.RegisterDTO;
import users.pojo.entity.UserProfile;
import users.pojo.entity.Users;
import users.pojo.vo.LoginResponse;
import users.service.AuthService;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class AuthServiceImpl extends ServiceImpl<UsersMapper, Users> implements AuthService {
    @Autowired
    UsersMapper usersMapper;
    @Autowired
    UserProfileMapper userProfileMapper;
    @Autowired
    NotificationClient notificationClient;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    Verification verification;

    @Override
    public Result<?> login(LoginDTO loginDTO) {
        // 参数校验（防止空指针异常）
        String userName = loginDTO.getUserName() == null ? "" : loginDTO.getUserName().trim();
        String key = loginDTO.getKey() == null ? "" : loginDTO.getKey().trim();
        String loginMethod = loginDTO.getLoginMethod();

        if (userName.isEmpty()) {
            log.warn("用户登录失败: 用户名为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "用户名不能为空");
        }

        // 校验登录方式
        if ((!"password".equals(loginMethod) && !"otp".equals(loginMethod))) {
            log.warn("用户登录失败: {} 登录方式无效", userName);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "登录方式无效");
        }

        // 根据登录方式校验 key 参数
        if ("password".equals(loginMethod)) {
            if (key.isEmpty()) {
                log.warn("用户登录失败: {} 密码为空", userName);
                return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "密码不能为空");
            }
        } else {
            if (key.isEmpty()) {
                log.warn("用户登录失败: {} 验证码为空", userName);
                return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码不能为空");
            }
        }

        Users user;
        // 查询用户（根据 userName 查 email 或 username）
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        if(!verification.isEmail(userName)){ // 管理员
            queryWrapper.eq("user_name", userName)
                    .and(i -> i.eq("deleted", 0));
        }else{
            queryWrapper.eq("email", userName)
                    .and(i -> i.eq("deleted", 0));
        }

        user = usersMapper.selectOne(queryWrapper);
        // 验证用户是否存在
        if (user == null) {
            log.warn("用户登录失败: {} 账户未注册", userName);
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), "账户未注册");
        }

        // 根据用户的登录方式进行校验
        if("otp".equals(loginMethod)){ // 验证码登录
            if(verification.isEmail(userName)){ // 非管理员
                String email = user.getEmail();
                if(email == null || email.isEmpty()){
                    log.warn("用户登录失败: {} 邮箱为空", userName);
                    return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱不存在");
                }
                // 从Redis中原子性验证并删除验证码（防止并发重复使用）
                String otpKey = RedisKey.LOGIN_OTP.getKey() + email;
                Boolean otpValid = redisUtil.verifyAndDeleteOtp(otpKey, key);
                if (!otpValid) {
                    log.warn("用户登录失败: {} 验证码错误或已过期", userName);
                    return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码错误或已过期");
                }
                log.info("用户验证码登录校验通过: {}", userName);
                redisUtil.del(otpKey);
            }else{
                log.warn("用户登录失败: {} 管理员不支持验证码登录", userName);
                return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "登录方式错误");
            }
        } else { // 密码登录
            // 验证密码
            if (!BCrypt.checkpw(key, user.getPassword())) {
                log.warn("用户登录失败: {} 密码错误", userName);
                return Result.error(ResultCode.PASSWORD_ERROR.getCode(), "密码错误");
            }
        }
        // 验证账号状态
        if (!Objects.equals(user.getStatus(), DictConstants.UserStatus.ACTIVE)) {
            log.warn("用户登录失败: {} 账号已被禁用", userName);
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用");
        }
        // 查询用户资料
        UserProfile userProfile = userProfileMapper.selectOne(new QueryWrapper<UserProfile>().eq("u_uid", user.getUUid()));
        if(userProfile == null){
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "用户资料不存在");
        }
        // 生成 accessToken 和 refreshToken
        AccessPayload accessPayload = new AccessPayload();
        accessPayload.setUUid(user.getUUid());
        accessPayload.setUserName(user.getUserName());
        accessPayload.setRole(user.getRole());
        accessPayload.setAvatar(userProfile.getAvatar());
        accessPayload.setNickName(userProfile.getNickName());
        accessPayload.setEmail(user.getEmail());
        accessPayload.setStatus(user.getStatus());
        accessPayload.setJit(UlidCreator.getUlid().toString());
        Map<String, Object> accessClaims = jwtUtil.setAccessClaims(accessPayload);
        String accessToken = jwtUtil.generateToken(accessClaims);
        long refreshTokenExpireTime = jwtUtil.accessExpiration*2;
        Boolean rememberMe = loginDTO.getRememberMe();
        if (rememberMe != null && rememberMe) {
            refreshTokenExpireTime = jwtUtil.remExpiration;
        }
        RefreshPayload refreshPayload = new RefreshPayload();
        refreshPayload.setUUid(user.getUUid());
        refreshPayload.setUserName(user.getUserName());
        refreshPayload.setJit(UlidCreator.getUlid().toString());
        Map<String, Object> refreshClaims = jwtUtil.setRefreshClaims(refreshPayload);
        String refreshToken = jwtUtil.generateToken(refreshClaims, refreshTokenExpireTime);
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        // 保存 refreshToken 到 redis 中（加入用户标识，防止同一JIT被不同用户使用）
        redisUtil.set(
                RedisKey.REFRESH_TOKEN.getKey() + user.getUUid() + ":" + refreshPayload.getJit(),
                refreshToken,
                refreshTokenExpireTime/1000
        );
        log.info("用户登录成功: {} 登录方式: {}", userName, loginMethod);
        return Result.success("登录成功", loginResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> register(RegisterDTO registerDTO) {
        // 去除空格（防止空指针异常）
        String email = registerDTO.getEmail() == null ? "" : registerDTO.getEmail().trim();
        String password = registerDTO.getPassword() == null ? "" : registerDTO.getPassword().trim();
        String otp = registerDTO.getOtp() == null ? "" : registerDTO.getOtp().trim();
        String userName = registerDTO.getUserName() == null ? "" : registerDTO.getUserName().trim();

        // 参数校验
        if (email.isEmpty()) {
            log.warn("用户注册失败: 邮箱为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱不能为空");
        }
        if (userName.isEmpty()) {
            log.warn("用户注册失败: 用户名为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "用户名不能为空");
        }
        if (password.isEmpty()) {
            log.warn("用户注册失败: 密码为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "密码不能为空");
        }
        if (otp.isEmpty()) {
            log.warn("用户注册失败: 验证码为空");
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码不能为空");
        }

        // 用户名长度校验
        if (userName.length() < 3 || userName.length() > 20) {
            log.warn("用户注册失败: {} 用户名长度必须在3-20个字符之间", userName);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "用户名长度必须在3-20个字符之间");
        }

        // 密码强度校验
        if (password.length() < 6 || password.length() > 20) {
            log.warn("用户注册失败: {} 密码长度必须在6-20个字符之间", userName);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "密码长度必须在6-20个字符之间");
        }

        // 邮箱格式校验（使用 trim 后的值）
        if(!verification.isEmail(email)){
            log.warn("用户注册失败: {} 邮箱格式错误", email);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱格式错误");
        }

        // 验证验证码（原子性验证并删除，防止并发重复使用）
        String otpKey = RedisKey.REGISTER_OTP.getKey() + email;
        Boolean otpValid = redisUtil.verifyAndDeleteOtp(otpKey, otp);
        if (!otpValid) {
            log.warn("用户注册失败: {} 验证码错误或已过期", email);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码错误或已过期");
        }
        redisUtil.del(otpKey);
        // 查询用户是否存在（合并查询）
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email)
                .or()
                .eq("user_name", userName);
        Users existingUser = usersMapper.selectOne(queryWrapper);
        if(existingUser != null){
            if(email.equals(existingUser.getEmail())){
                log.warn("用户注册失败: {} 用户已存在", email);
                return Result.error(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户已存在");
            }else{
                log.warn("用户注册失败: {} 用户名重复", userName);
                return Result.error(ResultCode.USER_ALREADY_EXISTS.getCode(), "用户名重复");
            }
        }

        // 生成密码哈希
        String ulidStr = ULIDUtils.generateULID();
        String pwdHash = BCrypt.hashpw(password, BCrypt.gensalt());

        // 填充用户信息
        Users user = new Users();
        user.setUUid(ulidStr);
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(pwdHash);
        user.setStatus(DictConstants.UserStatus.ACTIVE);
        user.setRole(DictConstants.UserRole.USER);
        int usersInsert = usersMapper.insert(user);
        if(usersInsert != 1){
            log.warn("用户注册失败: {} 用户插入失败", email);
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "用户插入失败");
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setUUid(ulidStr);
        int userProfileInsert = userProfileMapper.insert(userProfile);
        if(userProfileInsert != 1){
            log.warn("用户注册失败: {} 用户资料插入失败", email);
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "用户资料插入失败");
        }
        log.info("用户注册成功: {}", email);
        return Result.success("注册成功");
    }

    @Override
    public Result<?> forgotPassword(ForgotPasswordDTO forgotPasswordDTO) {
        String email = verification.trimStr(forgotPasswordDTO.getEmail());
        String otp = verification.trimStr(forgotPasswordDTO.getOtp());
        String password = verification.trimStr(forgotPasswordDTO.getNewPassword());
        if(email.isEmpty()){
            log.warn("用户忘记密码失败: {} 邮箱地址为空", email);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱地址为空");
        }
        if(otp.isEmpty()){
            log.warn("用户忘记密码失败: {} 验证码为空", email);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码为空");
        }
        if(password.isEmpty()){
            log.warn("用户忘记密码失败: {} 新密码为空", email);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "新密码为空");
        }
        // 验证验证码（原子性验证并删除，防止并发重复使用）
        String otpKey = RedisKey.FORGOT_PASSWORD_OTP.getKey() + email;
        Boolean otpValid = redisUtil.verifyAndDeleteOtp(otpKey, otp);
        if (!otpValid) {
            log.warn("用户忘记密码失败: {} 验证码错误或已过期", email);
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码错误或已过期");
        }
        redisUtil.del(otpKey);
        Wrapper<Users> wrapper = new QueryWrapper<Users>().eq("email", email);
        Users user = usersMapper.selectOne(wrapper);
        if(user == null){
            log.warn("用户忘记密码失败: {} 用户不存在", email);
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        String pwdHash = BCrypt.hashpw(password, BCrypt.gensalt());
        user.setPassword(pwdHash);
        int update = usersMapper.updateById(user);
        if(update != 1){
            log.warn("用户忘记密码失败: {} 更新密码失败", email);
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新密码失败");
        }
        log.info("用户忘记密码成功: {}", email);
        return Result.success("忘记密码成功");
    }
}