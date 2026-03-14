package common.interceptor;

import common.interfaces.AmountFormat;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jfr.Description;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Description("金额格式化拦截器，自动处理被@AmountFormat标记的字段")
public class AmountFormatInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理 Controller 中的方法（HandlerMethod 类型）
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取目标类的实例
            Object targetBean = handlerMethod.getBean();
            // 获取目标类的所有字段
            Field[] fields = targetBean.getClass().getDeclaredFields();

            for (Field field : fields) {
                // 判断字段是否被@AmountFormat注解标记
                if (field.isAnnotationPresent(AmountFormat.class)) {
                    // 设置私有字段可访问
                    field.setAccessible(true);
                    // 获取字段的值
                    Object fieldValue = field.get(targetBean);
                    // 仅处理BigDecimal类型的字段
                    if (fieldValue instanceof BigDecimal) {
                        AmountFormat annotation = field.getAnnotation(AmountFormat.class);
                        int scale = annotation.scale();
                        // 格式化金额：四舍五入，保留指定小数位数
                        BigDecimal formattedValue = ((BigDecimal) fieldValue).setScale(scale, RoundingMode.HALF_UP);
                        // 将格式化后的值设置回字段
                        field.set(targetBean, formattedValue);
                    }
                }
            }
        }
        // 返回true，继续执行后续拦截器和Controller方法
        return true;
    }
}
