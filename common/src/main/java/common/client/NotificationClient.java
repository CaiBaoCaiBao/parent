package common.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        contextId= ClientInfo.NotificationService.SERVICE_CONTEXT_ID,
        name= ClientInfo.NotificationService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface NotificationClient {
    @PostMapping("/notification/mail/trip-api/sendOtpMail")
    @Description(value = "发送验证码邮件")
    Result<?> sendOtpMail(
            @RequestParam String email,
            @RequestParam String template
    );
}
