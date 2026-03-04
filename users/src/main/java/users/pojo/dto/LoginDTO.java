package users.pojo.dto;

import lombok.Data;

@Data
public class LoginDTO {
    /**
     * 前台用户登录时，用户名为邮箱；
     * 管理员登录时，用户名为用户名
     */
    private String userName;
    /**
     * 密码
     */
    private String key;
    /**
     * 登录方式
     */
    private String loginMethod;
    /**
     * 是否是管理员
     */
    private Boolean adminFlag;
    /**
     * 是否记住我
     */
    private Boolean rememberMe;
}
