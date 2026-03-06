package common.utils;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

@Component
@Description("参数校验工具类")
public class Verification {
    /**
     * 判断字符串是否为邮箱格式
     * @param str 要检查的字符串
     * @return true=邮箱(普通用户), false=用户名(管理员)
     */
    public Boolean isEmail(String str){
        if (str == null || str.isEmpty()) {
            return false;
        }
        String emailRegex = "^(?!\\.)(?!.*\\.\\.)([a-z0-9_'+\\-\\.]*)[a-z0-9_+-]@([a-z0-9][a-z0-9\\-]*\\.)+[a-z]{2,}$";
        return str.matches(emailRegex);
    }
}
