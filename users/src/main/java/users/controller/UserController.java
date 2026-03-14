package users.controller;

import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import users.pojo.dto.*;
import users.service.UsersService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UsersService usersService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/create")
    @Description(value = "创建管理员")
    public Result<?> create(@RequestBody @Valid CreateAdminDTO createAdminDTO){
        return usersService.createAdmin(createAdminDTO);
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

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/info")
    @Description(value = "查询用户详情资料")
    public Result<?> detail(@RequestParam(value = "userName") String userName){
        return usersService.getUserInfoByUserName(userName);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新用户资料")
    public Result<?> updateUserInfo(@RequestBody SaveUserDTO saveUserDTO){
        return usersService.saveUser(saveUserDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update-status")
    @Description(value = "更新用户状态")
    public Result<?> updateStatus(@RequestBody @Valid UpdateUserStatusDTO updateUserStatusDTO){
        return usersService.updateUserStatus(updateUserStatusDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping(value = "/api/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Description(value = "上传头像")
    public Result<?> uploadAvatar(@RequestParam("file") MultipartFile file){
        return usersService.uploadAvatar(file);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/my-profile")
    @Description(value = "获取当前用户个人资料详情")
    public Result<?> getMyProfile(){
        return usersService.getMyProfile();
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/my-travel-notes")
    @Description(value = "获取当前用户发布的游记列表")
    public Result<?> getMyTravelNotes(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                       @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize){
        return usersService.getMyTravelNotes(pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/batch-info")
    @Description(value = "批量获取用户信息")
    public Result<?> getBatchUserInfo(@RequestParam("uids") List<String> uids){
        return usersService.getBatchUserInfo(uids);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/reset-password")
    @Description(value = "重置用户密码")
    public Result<?> resetPassword(@RequestBody @Valid ResetPasswordDTO resetPasswordDTO){
        return usersService.resetPassword(resetPasswordDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/count")
    @Description(value = "获取用户总数")
    public Result<?> getUserCount(){
        return usersService.getUserCount();
    }
}
