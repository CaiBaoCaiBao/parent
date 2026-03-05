package notification.service.impl;

import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import notification.service.MailService;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Description("邮件服务")
public class MailServiceImpl implements MailService {

    @Override
    public Result<?> sendOtpMail(String email, String otp, String template) {
        return null;
    }
}
