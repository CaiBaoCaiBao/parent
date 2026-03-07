package users.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import common.utils.Verification;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import users.mapper.UsersMapper;
import users.pojo.dto.DeleteUserDTO;
import users.pojo.dto.QueryUserListDTO;
import users.pojo.dto.SaveUserDTO;
import users.pojo.entity.Users;
import users.pojo.vo.UserListVo;
import users.service.UsersService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements UsersService {
    @Autowired
    UsersMapper usersMapper;
    @Autowired
    Verification verification;

    @Override
    public Result<?> saveUser(SaveUserDTO saveUserDTO) {
        String currentUserUUid = UserContext.getUserUUid();
        return null;
    }

    @Override
    public Result<?> deleteBatch(DeleteUserDTO deleteUserDTO) {
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        // 如果当前用户是管理员，则不进行操作
        if (Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "身份错误，无操作权限");
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
        return null;
    }
}
