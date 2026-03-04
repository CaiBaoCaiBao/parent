package users.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.JwtUtil;
import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import users.mapper.AuthMapper;
import users.pojo.dto.LoginDTO;
import users.pojo.entity.Users;
import users.service.AuthService;

@Slf4j
@Service
public class AuthServiceImpl extends ServiceImpl<AuthMapper, Users> implements AuthService {
    @Autowired
    AuthMapper authMapper;
    @Autowired
    JwtUtil jwtUtil;


    @Override
    public Result<?> login(LoginDTO loginDTO) {
        String userName = loginDTO.getUserName();
        String password = loginDTO.getKey();
        // TODO: 参数校验
        if (userName == null || userName.isEmpty()) {
            return Result.error(400,"用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            return Result.error(400,"密码不能为空");
        }

        // TODO: 查询用户（根据 userName 查 email 或 username）
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        if(loginDTO.getAdminFlag()){ // 管理员
            queryWrapper.eq("user_code", userName)
                    .and(i -> i.eq("deleted", 0));
        }else{
            queryWrapper.eq("email", userName)
                    .and(i -> i.eq("deleted", 0));
        }

        Users user = authMapper.selectOne(queryWrapper);
        // TODO: 验证用户是否存在
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND.getCode(),"用户不存在");
        }
        // TODO: 验证密码
        if (!BCrypt.checkpw(loginDTO.getKey(), user.getPassword())) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"密码错误");
        }
        // TODO: 验证账号状态
        if (user.getStatus() != DictConstants.UserStatus.ACTIVE) {
            return Result.error("账号已被禁用");
        }

//        String accessToken = jwtUtil.generateToken()
        return null;
    }
}
