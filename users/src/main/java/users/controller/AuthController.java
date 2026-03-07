package users.controller;

import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import users.pojo.dto.ForgotPasswordDTO;
import users.pojo.dto.LoginDTO;
import users.pojo.dto.RegisterDTO;
import users.service.AuthService;

@RestController
@RequestMapping("/users/auth")
@Description("认证功能接口")
public class AuthController {
    @Autowired
    private AuthService authService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/login")
    @Description(value = "用户登录")
    public Result<?> userLogin(@RequestBody @Valid LoginDTO loginDTO){
        return authService.login(loginDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/register")
    @Description(value = "前台用户注册")
    public Result<?> register(@RequestBody @Valid RegisterDTO registerDTO){
        return authService.register(registerDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/forgot-password")
    @Description(value = "前台用户注册")
    public Result<?> forgotPassword(@RequestBody @Valid ForgotPasswordDTO forgotPasswordDTO) {
        return authService.forgotPassword(forgotPasswordDTO);
    }
}
