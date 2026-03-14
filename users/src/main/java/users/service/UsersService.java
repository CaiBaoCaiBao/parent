package users.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;
import org.springframework.web.multipart.MultipartFile;
import users.pojo.dto.*;
import users.pojo.entity.Users;

import java.util.List;

public interface UsersService extends IService<Users> {
    @Description(value = "更新用户资料")
    Result<?> saveUser(@Valid SaveUserDTO saveUserDTO);

    @Description(value = "批量删除用户")
    Result<?> deleteBatch(DeleteUserDTO deleteUserDTO);

    @Description(value = "查询用户列表")
    Result<?> queryUserList(QueryUserListDTO queryUserListDTO);

    Result<?> getUserInfo(String uUid);

    @Description(value = "通过用户名获取用户信息")
    Result<?> getUserInfoByUserName(String userName);

    @Description(value = "通过用户ID获取用户信息")
    Result<?> getUserInfoByUUid(String uUid);

    @Description(value = "创建管理员")
    Result<?> createAdmin(@Valid CreateAdminDTO createAdminDTO);

    @Description(value = "更新用户状态")
    Result<?> updateUserStatus(@Valid UpdateUserStatusDTO updateUserStatusDTO);

    @Description(value = "上传头像")
    Result<?> uploadAvatar(MultipartFile file);

    @Description(value = "获取当前用户个人资料详情")
    Result<?> getMyProfile();

    @Description(value = "获取当前用户发布的游记列表")
    Result<?> getMyTravelNotes(Integer pageNum, Integer pageSize);

    @Description(value = "批量获取用户信息")
    Result<?> getBatchUserInfo(List<String> uids);

    @Description(value = "重置用户密码")
    Result<?> resetPassword(ResetPasswordDTO resetPasswordDTO);

    @Description(value = "获取用户总数")
    Result<?> getUserCount();
}
