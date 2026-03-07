package notification.controller;

import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import notification.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification/mail")
public class MailController {
    @Autowired
    MailService mailService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/trip-api/sendOtpMail")
    @Description("服务内调用发送验证码邮件")
    public Result<?> sendOtpMail(@RequestParam("email") String email,
                              @RequestParam("template") String template
    ) {
        return mailService.sendOtpMail(email, template);
    }
}
