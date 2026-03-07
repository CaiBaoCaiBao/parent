package users.service.impl;

import common.enums.ResultCode;
import common.utils.Result;
import common.utils.Verification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import users.client.NotificationClient;
import users.pojo.dto.SendOtpMailDTO;
import users.service.MailService;

@Slf4j
@Service
public class MailServiceImpl implements MailService {
    @Autowired
    NotificationClient notificationClient;
    @Autowired
    private Verification verification;


    @Override
    public Result<?> sendOtpMail(SendOtpMailDTO dto) {
        String email = verification.trimStr(dto.getEmail());
        String template = verification.trimStr(dto.getTemplate());
        if (email == null || email.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱不能为空");
        }
        if (template == null || template.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "验证码类型错误");
        }
        if (!verification.isEmail(email)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "邮箱格式不正确");
        }
        return notificationClient.sendOtpMail(email, template);
    }
}
