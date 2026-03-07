package users.controller;

import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import users.pojo.dto.SendOtpMailDTO;
import users.service.MailService;

@RestController
@RequestMapping("/users/mail")
@Description("邮件功能接口")
public class MailController {
    @Autowired
    MailService mailService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/sendOtp")
    @Description(value = "获取OTP验证码")
    public Result<?> sendOtp(@RequestBody @Valid SendOtpMailDTO dto){
        return mailService.sendOtpMail(dto);
    }
}
