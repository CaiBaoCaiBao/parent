package users.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.client.FileClient;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.PinyinUtil;
import common.utils.Result;
import common.utils.ULIDUtils;
import common.utils.Verification;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import users.mapper.UserProfileMapper;
import users.mapper.UsersMapper;
import users.pojo.dto.*;
import users.pojo.entity.UserProfile;
import users.pojo.entity.Users;
import users.pojo.vo.UserInfoVo;
import users.pojo.vo.UserListVo;
import users.service.UsersService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
        int profileFlag = userProfileMapper.updateByUid(userProfile);
        if(profileFlag != 1){
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新资料失败");
        }
        return Result.success("更新成功");
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
        List<String> uUids = deleteUserDTO.getUUids();
        // 从列表中排除当前用户
        List<String> filteredUUids = uUids.stream()
                .filter(uUid -> !uUid.equals(currentUserUUid))
                .toList();
        // 如果过滤后为空，说明要删除的都是自己
        if (filteredUUids.isEmpty()) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(),"不能对自己执行此操作");
        }
        // 执行批量删除
        boolean result = remove(new QueryWrapper<Users>().in("u_uid", filteredUUids));
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
        List<UserListVo> userListVos = usersMapper.queryUserList(queryUserListDTO);
        return Result.success(userListVos);
    }

    @Override
    public Result<?> getUserInfo(String uUid) {
        // 检查当前用户状态
        String currentStatus = UserContext.getStatus();
        if(Objects.equals(currentStatus, DictConstants.UserStatus.INACTIVE)){ // 用户状态为禁用，无法查询
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(),"账号已被禁用，无法执行此操作");
        }
        // 先查询用户信息
        UserInfoVo userInfoVo = usersMapper.getUserInfo(uUid);
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
        String ulidStr = ULIDUtils.generateULID();
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
        // 检查用户状态是否合法
        if (!DictConstants.UserStatus.ACTIVE.equals(updateUserStatusDTO.getStatus())) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账户处于禁用状态，无法执行此操作");
        }
        // 检查updateUserStatusDTO的uUid是否存在
        Wrapper<Users> queryWrapper = new QueryWrapper<Users>().eq("u_uid", updateUserStatusDTO.getUUid());;
        Users users = usersMapper.selectOne(queryWrapper);
        if (users == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "用户不存在");
        }
        // 更新用户状态
        users.setStatus(updateUserStatusDTO.getStatus());
        int userFlag = usersMapper.updateById(users);
        if(userFlag != 1){
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新用户状态失败");
        }
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
            return Result.success("更新用户头像");
        } else {
            return fileResult;
        }
    }
}
