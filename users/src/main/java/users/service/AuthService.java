package users.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;
import users.pojo.dto.ForgotPasswordDTO;
import users.pojo.dto.LoginDTO;
import users.pojo.dto.RegisterDTO;
import users.pojo.entity.Users;

public interface AuthService extends IService<Users> {
    @Description("登录")
    Result<?> login(@Valid LoginDTO loginDTO);

    @Description("注册")
    Result<?> register(@Valid RegisterDTO registerDTO);

    @Description("忘记密码")
    Result<?> forgotPassword(@Valid ForgotPasswordDTO forgotPasswordDTO);

    @Description("刷新Token")
    Result<?> refreshToken(String authorization);
}
