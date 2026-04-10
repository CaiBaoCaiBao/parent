package users.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.client.ContentClient;
import common.client.FileClient;
import common.common.AccessPayload;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.JwtUtil;
import common.utils.PinyinUtil;
import common.utils.Result;
import common.utils.ULIDUtils;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import users.mapper.UserProfileMapper;
import users.mapper.UsersMapper;
import users.pojo.dto.*;
import users.pojo.entity.UserProfile;
import users.pojo.entity.Users;
import users.pojo.vo.LoginResponse;
import users.pojo.vo.UserInfoVo;
import users.pojo.vo.UserListVo;
import users.service.UsersService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {
    @Autowired
    UsersMapper usersMapper;
    @Autowired
    UserProfileMapper userProfileMapper;
    @Autowired
    FileClient fileClient;
    @Autowired
    ContentClient contentClient;
    @Autowired
    JwtUtil jwtUtil;

    @Override
    public Result<?> saveUser(SaveUserDTO saveUserDTO) {
        String currentUserUUid = UserContext.getUserUUid();
        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }
        Wrapper<Users> wrapper = new QueryWrapper<Users>().eq("u_uid", currentUserUUid);
        Users currentUser = usersMapper.selectOne(wrapper);
        if (currentUser == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        if (currentUser.getStatus().equals(DictConstants.UserStatus.INACTIVE)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }
        UserProfile  userProfile = new UserProfile();
        userProfile.setUUid(currentUserUUid);
        userProfile.setNickName(saveUserDTO.getNickName());
        userProfile.setAvatar(saveUserDTO.getAvatar());
        userProfile.setBio(saveUserDTO.getBio());
        userProfile.setPhone(saveUserDTO.getPhone());

        // 处理生日字段转换
        userProfile.setBirthday(saveUserDTO.getBirthday());

        int profileFlag = userProfileMapper.updateByUid(userProfile);
        if(profileFlag != 1){
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新资料失败");
        }

        // 重新生成 accessToken
        AccessPayload accessPayload = new AccessPayload();
        accessPayload.setUUid(currentUser.getUUid());
        accessPayload.setUserName(currentUser.getUserName());
        accessPayload.setRole(currentUser.getRole());
        accessPayload.setAvatar(saveUserDTO.getAvatar());
        accessPayload.setNickName(saveUserDTO.getNickName());
        accessPayload.setEmail(currentUser.getEmail());
        accessPayload.setStatus(currentUser.getStatus());
        accessPayload.setJit(UlidCreator.getUlid().toString());

        Map<String, Object> accessClaims = jwtUtil.setAccessClaims(accessPayload);
        String newAccessToken = jwtUtil.generateToken(accessClaims);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(newAccessToken);

        return Result.success("更新成功", loginResponse);
    }

    @Override
    public Result<?> deleteBatch(DeleteUserDTO deleteUserDTO) {
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        // 如果当前用户是管理员，则不进行操作
        if (Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "无操作权限");
        }
        if (Objects.equals(status, DictConstants.UserStatus.INACTIVE)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }
        String currentUserUUid = UserContext.getUserUUid();
        List<String> uuids = deleteUserDTO.getUuids();
        // 从列表中排除当前用户
        List<String> filteredUuids = uuids.stream()
                .filter(uuid -> !uuid.equals(currentUserUUid))
                .toList();
        // 如果过滤后为空，说明要删除的都是自己
        if (filteredUuids.isEmpty()) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(),"不能对自己执行此操作");
        }
        // 执行批量删除
        boolean result = remove(new QueryWrapper<Users>().in("u_uid", filteredUuids));
        if (!result) {
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(),"删除失败");
        }
        return Result.success("删除成功");
    }

    @Override
    public Result<?> queryUserList(QueryUserListDTO queryUserListDTO) {
        String currentRole = UserContext.getRole();
        String currentStatus = UserContext.getStatus();
        if(Objects.equals(queryUserListDTO.getRole(), DictConstants.UserRole.ADMIN) &&
                Objects.equals(currentRole, DictConstants.UserRole.USER)){ // 用户角色不能查询管理员
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"身份错误，无操作权限");
        }
        if(Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)){ // 用户状态为禁用，无法查询
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(),"账号已被禁用，无法执行此操作");
        }

        // 设置默认分页参数
        if (queryUserListDTO.getPageNum() == null || queryUserListDTO.getPageNum() < 1) {
            queryUserListDTO.setPageNum(1);
        }
        if (queryUserListDTO.getPageSize() == null || queryUserListDTO.getPageSize() < 1) {
            queryUserListDTO.setPageSize(10);
        }

        // 查询用户列表
        List<UserListVo> userListVos = usersMapper.queryUserList(queryUserListDTO);

        // 添加调试日志
        if (!userListVos.isEmpty()) {
            UserListVo firstUser = userListVos.get(0);
            log.info("查询到 {} 个用户", userListVos.size());
            log.info("第一个用户 - uUid: {}, userName: {}, email: {}", 
                firstUser.getUUid(), firstUser.getUserName(), firstUser.getEmail());
        }

        // 构建分页结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", userListVos);
        result.put("total", userListVos.size());
        result.put("size", queryUserListDTO.getPageSize());
        result.put("current", queryUserListDTO.getPageNum());
        result.put("pages", (int) Math.ceil((double) userListVos.size() / queryUserListDTO.getPageSize()));

        return Result.success(result);
    }

    @Override
    public Result<?> getUserInfo(String uUid) {
        // 检查当前用户状态
        String currentStatus = UserContext.getStatus();
        if(Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)){ // 用户状态为禁用，无法查询
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(),"账号已被禁用，无法执行此操作");
        }
        // 先查询用户信息（使用 uUid 查询）
        UserInfoVo userInfoVo = usersMapper.getUserInfoByUUid(uUid);
        // 检查用户是否存在
        if (userInfoVo == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        String currentRole = UserContext.getRole();
        // 用户角色不能查询管理员
        if(Objects.equals(userInfoVo.getRole(), DictConstants.UserRole.ADMIN) &&
                Objects.equals(currentRole, DictConstants.UserRole.USER)){
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"没有权限查看该用户");
        }
        return Result.success(userInfoVo);
    }

    @Override
    public Result<?> getUserInfoByUserName(String userName) {
        // 检查当前用户状态
        String currentStatus = UserContext.getStatus();
        if(Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)){ // 用户状态为禁用，无法查询
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(),"账号已被禁用，无法执行此操作");
        }
        // 先查询用户信息（使用 userName 查询）
        UserInfoVo userInfoVo = usersMapper.getUserInfoByUserName(userName);
        // 检查用户是否存在
        if (userInfoVo == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        String currentRole = UserContext.getRole();
        // 用户角色不能查询管理员
        if(Objects.equals(userInfoVo.getRole(), DictConstants.UserRole.ADMIN) &&
                Objects.equals(currentRole, DictConstants.UserRole.USER)){
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"没有权限查看该用户");
        }
        return Result.success(userInfoVo);
    }

    @Override
    public Result<?> getUserInfoByUUid(String uUid) {
        String currentStatus = UserContext.getStatus();
        if(Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)){
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(),"账号已被禁用，无法执行此操作");
        }
        UserInfoVo userInfoVo = usersMapper.getUserInfoByUUid(uUid);
        if (userInfoVo == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        String currentRole = UserContext.getRole();
        if(Objects.equals(userInfoVo.getRole(), DictConstants.UserRole.ADMIN) &&
                Objects.equals(currentRole, DictConstants.UserRole.USER)){
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"没有权限查看该用户");
        }
        return Result.success(userInfoVo);
    }

    @Override
    public Result<?> createAdmin(CreateAdminDTO createAdminDTO) {
        String currentUserUUid = UserContext.getUserUUid();
        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }
        Wrapper<Users> wrapper = new QueryWrapper<Users>().eq("u_uid", currentUserUUid);
        Users currentUser = usersMapper.selectOne(wrapper);
        if (currentUser == null || !currentUser.getRole().equals(DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "身份错误，无操作权限");
        }
        // 限制只能创建管理员角色
        if (!DictConstants.UserRole.ADMIN.equals(createAdminDTO.getRole())) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "创建角色不合法");
        }
        // 检查邮箱是否已存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", createAdminDTO.getEmail());
        if (usersMapper.selectCount(queryWrapper) > 0) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱地址已存在");
        }
        String ulidStr = "USER_" + ULIDUtils.generateULID();
        String namePinyin = PinyinUtil.toPinyin(createAdminDTO.getUserName());
        // 格式化当前日期
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String nowDate = LocalDate.now().format(formatter);
        // 生成密码哈希
        String pwdHash = BCrypt.hashpw(namePinyin+"_"+nowDate, BCrypt.gensalt());
        Users users = new Users();
        users.setUUid(ulidStr);
        users.setUserName(createAdminDTO.getUserName());
        users.setEmail(createAdminDTO.getEmail());
        users.setPassword(pwdHash);
        users.setRole(createAdminDTO.getRole());
        users.setStatus(DictConstants.UserStatus.ACTIVE);
        int usersInsert = usersMapper.insert(users);
        if(usersInsert != 1){
            log.warn("用户: {} 创建管理员失败", currentUserUUid);
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "创建管理员失败");
        }
        UserProfile userProfile = new UserProfile();
        userProfile.setUUid(ulidStr);
        int userProfileInsert = userProfileMapper.insert(userProfile);
        if(userProfileInsert != 1){
            log.warn("创建管理员{}资料失败", ulidStr);
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "创建资料失败");
        }
        log.info("用户: {}创建管理员:{}", currentUserUUid,ulidStr);
        return Result.success("创建成功");
    }

    @Override
    public Result<?> updateUserStatus(UpdateUserStatusDTO updateUserStatusDTO) {
        log.info("updateUserStatusDTO: {}", updateUserStatusDTO);
        // 查询当前用户是否存在
        String currentUserUUid = UserContext.getUserUUid();
        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }
        Wrapper<Users> wrapper = new QueryWrapper<Users>().eq("u_uid", currentUserUUid);
        Users currentUser = usersMapper.selectOne(wrapper);
        if (currentUser == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        // 检查当前用户是否是管理员
        if (!currentUser.getRole().equals(DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "身份错误，无操作权限");
        }
        // 检查用户状态是否合法（使用数据库中的最新状态）
        if (!DictConstants.UserStatus.ACTIVE.equals(currentUser.getStatus())) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "您的账户已被禁用，无法执行此操作。请联系其他管理员恢复您的账户状态。");
        }
        // 检查是否尝试禁用自己
        if (currentUserUUid.equals(updateUserStatusDTO.getUuid())) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "不能禁用自己的账户");
        }
        // 检查updateUserStatusDTO的uuid是否存在
        log.info("查询目标用户，UUID: {}", updateUserStatusDTO.getUuid());
        Wrapper<Users> queryWrapper = new QueryWrapper<Users>().eq("u_uid", updateUserStatusDTO.getUuid());
        Users users = usersMapper.selectOne(queryWrapper);
        log.info("查询结果: {}", users);
        if (users == null) {
            log.warn("目标用户不存在，UUID: {}", updateUserStatusDTO.getUuid());
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "目标用户不存在");
        }
        // 检查目标用户是否是管理员（防止禁用其他管理员）
        if (users.getRole().equals(DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "不能禁用其他管理员账户");
        }
        // 验证状态值是否合法
        if (!DictConstants.UserStatus.ACTIVE.equals(updateUserStatusDTO.getStatus()) &&
            !DictConstants.UserStatus.INACTIVE.equals(updateUserStatusDTO.getStatus())) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "状态值无效，必须是 active 或 inactive");
        }
        // 更新用户状态
        users.setStatus(updateUserStatusDTO.getStatus());
        int userFlag = usersMapper.updateById(users);
        if(userFlag != 1){
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新用户状态失败");
        }
        log.info("管理员 {} 将用户 {} 的状态更新为 {}", currentUserUUid, updateUserStatusDTO.getUuid(), updateUserStatusDTO.getStatus());
        return Result.success("用户状态更新成功");
    }

    @Override
    public Result<?> uploadAvatar(MultipartFile file) {
        // 查询当前用户是否存在
        String currentUserUUid = UserContext.getUserUUid();
        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }
        Wrapper<Users> wrapper = new QueryWrapper<Users>().eq("u_uid", currentUserUUid);
        Users currentUser = usersMapper.selectOne(wrapper);
        if (currentUser == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        Result<?> fileResult =  fileClient.uploadImg(file);
        // 判断调用是否成功
        if (fileResult != null && fileResult.getSuccess()) {
            // 获取返回的文件URL
            String fileUrl = (String) fileResult.getData();
            Wrapper<UserProfile> queryWrapper = new QueryWrapper<UserProfile>().eq("u_uid", currentUserUUid);
            UserProfile userProfile = userProfileMapper.selectOne(queryWrapper);
            userProfile.setAvatar(fileUrl);
            int userProfileFlag = userProfileMapper.updateById(userProfile);
            if(userProfileFlag != 1){
                return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新用户头像失败");
            }
            log.info("上传成功: {}", fileUrl);
            return Result.success("更新用户头像",fileUrl);
        } else {
            return fileResult;
        }
    }

    @Override
    public Result<?> getMyProfile() {
        String currentUserUUid = UserContext.getUserUUid();
        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }
        return getUserInfoByUUid(currentUserUUid);
    }

    @Override
    public Result<?> getMyTravelNotes(Integer pageNum, Integer pageSize) {
        String currentUserUUid = UserContext.getUserUUid();
        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }
        String currentStatus = UserContext.getStatus();
        if (Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }
        // 调用内容服务查询当前用户的游记列表
        Result<?> result = contentClient.queryTravelNoteList(currentUserUUid, pageNum, pageSize);
        return result;
    }

    @Override
    public Result<?> getBatchUserInfo(List<String> uids) {
        if (uids == null || uids.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "用户ID列表不能为空");
        }
        // 批量查询用户信息
        List<UserInfoVo> userInfoList = usersMapper.getBatchUserInfo(uids);
        return Result.success(userInfoList);
    }

    @Override
    public Result<?> resetPassword(ResetPasswordDTO resetPasswordDTO) {
        // 获取当前登录用户信息
        String currentUserUUid = UserContext.getUserUUid();
        String currentRole = UserContext.getRole();
        String currentStatus = UserContext.getStatus();

        if (currentUserUUid == null || currentUserUUid.isEmpty()) {
            return Result.error(ResultCode.UNAUTHORIZED.getCode(), "身份错误，无操作权限");
        }

        // 只有管理员才能重置密码
        if (!DictConstants.UserRole.ADMIN.equals(currentRole)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "只有管理员才能重置密码");
        }

        // 检查当前用户状态
        if (Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }

        // 验证两次密码是否一致
        if (!resetPasswordDTO.getNewPassword().equals(resetPasswordDTO.getConfirmPassword())) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "两次输入的密码不一致");
        }

        // 查询目标用户
        QueryWrapper<Users> wrapper = new QueryWrapper<Users>().eq("u_uid", resetPasswordDTO.getUuid());
        Users targetUser = usersMapper.selectOne(wrapper);
        if (targetUser == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "目标用户不存在");
        }

        // 检查是否尝试重置自己的密码
        if (currentUserUUid.equals(resetPasswordDTO.getUuid())) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "不能重置自己的密码，请使用修改密码功能");
        }

        // 检查目标用户是否是管理员
        if (targetUser.getRole().equals(DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "不能重置其他管理员的密码");
        }

        // 生成新密码的哈希值
        String pwdHash = BCrypt.hashpw(resetPasswordDTO.getNewPassword(), BCrypt.gensalt());
        targetUser.setPassword(pwdHash);

        // 更新密码
        int updateFlag = usersMapper.updateById(targetUser);
        if (updateFlag != 1) {
            log.warn("重置密码失败: 用户ID {}", resetPasswordDTO.getUuid());
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "重置密码失败");
        }

        log.info("管理员 {} 重置了用户 {} 的密码", currentUserUUid, resetPasswordDTO.getUuid());
        return Result.success("重置密码成功");
    }

    @Override
    public Result<?> getUserCount() {
        // 查询用户总数（内部服务调用，无需权限验证）
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        Long userCount = usersMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("userCount", userCount);

        return Result.success(result);
    }

    @Override
    public Result<?> getMonthlyUserCount() {
        // 获取本月第一天和最后一天
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // 查询本月新增用户数（内部服务调用，无需权限验证）
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", firstDayOfMonth.atStartOfDay());
        wrapper.le("created_at", lastDayOfMonth.atTime(23, 59, 59));
        Long monthlyUserCount = usersMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyUserCount", monthlyUserCount);

        return Result.success(result);
    }

    @Override
    public Result<?> getLastMonthlyUserCount() {
        // 获取上月第一天和最后一天
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = now.withDayOfMonth(1).minusDays(1);

        // 查询上月新增用户数（内部服务调用，无需权限验证）
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", firstDayOfLastMonth.atStartOfDay());
        wrapper.le("created_at", lastDayOfLastMonth.atTime(23, 59, 59));
        Long lastMonthlyUserCount = usersMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("lastMonthlyUserCount", lastMonthlyUserCount);

        return Result.success(result);
    }

    @Override
    public Result<?> getMonthlyUserCountByMonth(int year, int month) {
        // 获取指定月份的第一天和最后一天
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

        // 查询指定月份的新增用户数（内部服务调用，无需权限验证）
        QueryWrapper<Users> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", firstDayOfMonth.atStartOfDay());
        wrapper.le("created_at", lastDayOfMonth.atTime(23, 59, 59));
        Long monthlyUserCount = usersMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyUserCount", monthlyUserCount);

        return Result.success(result);
    }
}
