package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description(value = "前台用户注册DTO")
public class RegisterDTO {
    private String userName;
    private String email;
    private String password;
    private String otp;
}
