package users.controller;

import common.enums.RedisKey;
import common.enums.ResultCode;
import common.utils.RedisUtil;
import common.utils.Result;
import common.utils.StrGenerator;
import common.utils.Verification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import users.client.NotificationClient;
import users.pojo.dto.SendOtpMailDTO;

@RestController
@RequestMapping("/users/mail")
public class MailController {
    @Autowired
    NotificationClient notificationClient;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/sendOtp")
    @Description(value = "获取OTP验证码")
    public Result<?> sendOtp(@RequestBody SendOtpMailDTO dto){
        return notificationClient.sendOtpMail(dto.getEmail(), dto.getTemplate());
    }
}
