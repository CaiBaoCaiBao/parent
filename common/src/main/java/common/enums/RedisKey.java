package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.context.annotation.Description;

@Getter
@AllArgsConstructor
@Description(value = "Redis Key枚举")
public enum RedisKey {
    /**
     * 刷新Token
     */
    REFRESH_TOKEN("token:refresh:", "刷新Token"),
    /**
     * 黑名单Token
     */
    BLACK_LIST_TOKEN("token:black:", "黑名单Token"),
    /**
     * 登录OTP验证码
     */
    LOGIN_OTP("otp:login:", "登录OTP验证码"),
    /**
     * 注册OTP验证码
     */
    REGISTER_OTP("otp:register:", "注册OTP验证码"),
    /**
     * 忘记密码OTP验证码
     */
    FORGOT_PASSWORD_OTP("otp:forgot-pwd:", "忘记密码OTP验证码");

    private final String key;
    private final String desc;
}
