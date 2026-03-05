package users.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        contextId= ClientInfo.NotificationService.SERVICE_CONTEXT_ID,
        name= ClientInfo.NotificationService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface NotificationClient {
    @PostMapping("/mail/trip-api/sendOtpMail")
    @Description(value = "发送验证码邮件")
    String sendOtpMail(String email, String otp, String template);
}
