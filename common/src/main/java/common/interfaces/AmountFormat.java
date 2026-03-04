package common.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 金额格式化注解，用于标记需要统一处理小数位数的字段
 */
@Retention(RetentionPolicy.RUNTIME)  // 注解在运行时生效，允许反射获取
@Target(ElementType.FIELD)           // 注解仅作用于类的字段
public @interface AmountFormat {
    /**
     * 保留小数位数，默认2位
     */
    int scale() default 2;
}