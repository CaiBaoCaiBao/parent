package notification.enums;

import common.enums.RedisKey;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MailTemplate {
    REGISTER("register", RedisKey.REGISTER_OTP.getKey(), "注册验证码", "register.html"),
    LOGIN("login", RedisKey.LOGIN_OTP.getKey(),"登录验证码", "login.html"),
    ;

    private final String code;
    private final String redisKey;
    private final String subject;
    private final String templatePath;

    public static MailTemplate fromCode(String code) {
        for (MailTemplate template : values()) {
            if (template.getCode().equals(code)) {
                return template;
            }
        }
        throw new IllegalArgumentException("无效的模板类型: " + code);
    }
}
