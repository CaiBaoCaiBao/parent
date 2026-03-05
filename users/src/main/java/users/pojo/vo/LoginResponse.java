package users.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("登录响应数据")
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
}
