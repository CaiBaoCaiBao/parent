package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("发送邮件参数")
public class SendOtpMailDTO {
    private String email;
    private String template;
}
