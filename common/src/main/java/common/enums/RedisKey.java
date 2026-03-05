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
    BLACK_LIST_TOKEN("token:black:", "黑名单Token");

    private final String key;
    private final String desc;
}
