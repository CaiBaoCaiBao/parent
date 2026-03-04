package users.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;
import users.pojo.dto.LoginDTO;
import users.pojo.entity.Users;

public interface AuthService extends IService<Users> {
    @Description("登录")
    Result<?> login(@Valid LoginDTO loginDTO);
}
