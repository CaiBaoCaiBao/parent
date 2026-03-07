package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("忘记密码DTO")
public class ForgotPasswordDTO {
    private String email;
    private String newPassword;
    private String otp;
}
