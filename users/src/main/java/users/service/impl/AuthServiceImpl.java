package users.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.f4b6a3.ulid.UlidCreator;
import common.common.AccessPayload;
import common.common.RefreshPayload;
import common.dict.DictConstants;
import common.enums.RedisKey;
import common.enums.ResultCode;
import common.utils.JwtUtil;
import common.utils.RedisUtil;
import common.utils.Result;
import common.utils.Verification;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import users.client.NotificationClient;
import users.mapper.AuthMapper;
import users.mapper.UserProfileMapper;
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
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Users> implements AuthService {
    @Autowired
    AuthMapper authMapper;
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
        String userName = loginDTO.getUserName().trim();
        String password = loginDTO.getKey().trim();
        // TODO: 参数校验
        if (userName.isEmpty()) {
            log.warn("用户登录失败: {} 用户名为空", userName);
            return Result.error(400,"用户名不能为空");
        }
        if (password.isEmpty()) {
            log.warn("用户登录失败: {} 密码为空", userName);
            return Result.error(400,"密码不能为空");
        }
        Users user;
        // TODO: 查询用户（根据 userName 查 email 或 username）
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        if(!verification.isEmail(userName)){ // 管理员
            queryWrapper.eq("user_name", userName)
                    .and(i -> i.eq("deleted", 0));
        }else{
            queryWrapper.eq("email", userName)
                    .and(i -> i.eq("deleted", 0));
        }

        user = authMapper.selectOne(queryWrapper);
        // TODO: 验证用户是否存在
        if (user == null) {
            log.warn("用户登录失败: {} 账户未注册", userName);
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(),"账户未注册");
        }

        // TODO: 根据用户的登录方式进行校验
        if(loginDTO.getLoginMethod().equals("otp")){ // 验证码登录
            if(verification.isEmail(userName)){ // 非管理员
                String email = user.getEmail();
                if(email == null || email.isEmpty()){
                    log.warn("用户登录失败: {} 邮箱为空", userName);
                    return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"邮箱不存在");
                }
                String storedOtp = redisUtil.get(RedisKey.LOGIN_OTP.getKey() + email).toString();
                if(storedOtp == null || !storedOtp.equals(loginDTO.getKey())){
                    log.warn("用户登录失败: {} 验证码错误或已过期", userName);
                    return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"验证码错误或已过期");
                }
                // 验证码校验通过后，删除Redis中的OTP，防止重复使用
                redisUtil.del(RedisKey.LOGIN_OTP.getKey() + email);
                log.info("用户验证码登录校验通过: {}", userName);
            }else{
                log.warn("用户登录失败: {} 管理员不支持验证码登录", userName);
                return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"登录方式错误");
            }
        } else { // 密码登录
            // TODO: 验证密码
            if (!BCrypt.checkpw(loginDTO.getKey(), user.getPassword())) {
                log.warn("用户登录失败: {} 密码错误", userName);
                return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"密码错误");
            }
        }
        // TODO: 验证账号状态
        if (!Objects.equals(user.getStatus(), DictConstants.UserStatus.ACTIVE)) {
            log.warn("用户登录失败: {} 账号已被禁用", userName);
            return Result.error("账号已被禁用");
        }
        // 查询用户资料
        UserProfile userProfile = userProfileMapper.selectOne(new QueryWrapper<UserProfile>().eq("u_uid", user.getUUid()));
        if(userProfile == null){
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"用户资料不存在");
        }
        // TODO: 生成 accessToken 和 refreshToken
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
        // TODO: 保存 refreshToken 到 redis 中（加入用户标识，防止同一JIT被不同用户使用）
        redisUtil.set(
                RedisKey.REFRESH_TOKEN.getKey() + user.getUUid() + ":" + refreshPayload.getJit(),
                refreshToken,
                refreshTokenExpireTime/1000
        );
        log.info("用户登录成功: {}", userName);
        return Result.success("登录成功",loginResponse);
    }

    @Override
    public Result<?> register(RegisterDTO registerDTO) {
        return null;
    }
}