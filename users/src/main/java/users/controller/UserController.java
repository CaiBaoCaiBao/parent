package users.controller;

import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import users.pojo.dto.DeleteUserDTO;
import users.pojo.dto.QueryUserListDTO;
import users.pojo.dto.SaveUserDTO;
import users.service.UsersService;

@RestController
@RequestMapping("/users/user")
public class UserController {
    @Autowired
    UsersService usersService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/save")
    @Description(value = "保存用户信息")
    public Result<?> save(@RequestBody @Valid SaveUserDTO saveUserDTO){
        return usersService.saveUser(saveUserDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/admin-api/batch-delete")
    @Description(value = "批量删除用户")
    public Result<?> batchDelete(@RequestBody DeleteUserDTO deleteUserDTO){
        return usersService.deleteBatch(deleteUserDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询用户列表")
    public Result<?> list(@ModelAttribute QueryUserListDTO queryUserListDTO){
        return usersService.queryUserList(queryUserListDTO);
    }
}
