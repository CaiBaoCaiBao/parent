package users.controller;

import common.enums.ResultCode;
import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import users.pojo.dto.LoginDTO;
import users.pojo.dto.RegisterDTO;
import users.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/login")
    @Description(value = "前台用户登录")
    public Result<?> userLogin(@RequestBody @Valid LoginDTO loginDTO){
        if (loginDTO.getAdminFlag() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"无法识别身份，缺少身份标识参数");
        }
        loginDTO.setAdminFlag(false);
        return authService.login(loginDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/login")
    @Description(value = "管理员登录")
    public Result<?> adminLogin(@RequestBody @Valid LoginDTO loginDTO){
        if (loginDTO.getAdminFlag() == null) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),"无法识别身份，缺少身份标识参数");
        }
        loginDTO.setAdminFlag(true);
        return authService.login(loginDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/register")
    @Description(value = "前台用户注册")
    public Result<?> register(@RequestBody @Valid RegisterDTO registerDTO){
        return authService.register(registerDTO);
    }
}
