package users.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;
import users.pojo.dto.CreateAdminDTO;
import users.pojo.dto.DeleteUserDTO;
import users.pojo.dto.QueryUserListDTO;
import users.pojo.dto.SaveUserDTO;
import users.pojo.entity.Users;

public interface UsersService extends IService<Users> {
    @Description(value = "保存用户信息")
    Result<?> saveUser(@Valid SaveUserDTO saveUserDTO);

    @Description(value = "批量删除用户")
    Result<?> deleteBatch(DeleteUserDTO deleteUserDTO);

    @Description(value = "查询用户列表")
    Result<?> queryUserList(QueryUserListDTO queryUserListDTO);

    Result<?> getUserInfo(String uUid);

    @Description(value = "创建管理员")
    Result<?> createAdmin(@Valid CreateAdminDTO createAdminDTO);
}
