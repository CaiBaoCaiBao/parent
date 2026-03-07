package users.service;

import common.utils.Result;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;
import users.pojo.dto.SendOtpMailDTO;

public interface MailService {
    @Description(value = "发送OTP邮件")
    Result<?> sendOtpMail(@Valid SendOtpMailDTO dto);
}
