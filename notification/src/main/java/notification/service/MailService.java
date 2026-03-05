package notification.service;

import common.utils.Result;

public interface MailService {
    Result<?> sendOtpMail(String email, String otp, String template);
}
