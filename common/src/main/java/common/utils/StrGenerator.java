package common.utils;

import org.springframework.context.annotation.Description;

import java.security.SecureRandom;
import java.util.Random;

public class StrGenerator {
    private static final String UPPER_CASE = "QWERTYUPASDFGHJKLZXCVBNM";
    private static final String LOWER_CASE = "qwertyuipasdfghjkzxcvbnm";
    private static final String DIGITS = "23456789";
    private static final String ALL_CHARS = UPPER_CASE + LOWER_CASE + DIGITS;
    private static final Random RANDOM = new SecureRandom();

    @Description("纯数字字符串")
    public static String generateNumericStr(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码的长度必须大于0");
        }

        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(DIGITS.length());
            otp.append(DIGITS.charAt(index));
        }
        return otp.toString();
    }

    @Description("字母数字混合字符串")
    public static String generateAlphanumericStr(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码的长度必须大于0");
        }

        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(ALL_CHARS.length());
            otp.append(ALL_CHARS.charAt(index));
        }
        return otp.toString();
    }
}
