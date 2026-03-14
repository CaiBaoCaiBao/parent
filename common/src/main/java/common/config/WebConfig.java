package common.config;

import common.interceptor.AmountFormatInterceptor;
import common.interceptor.UserContextInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserContextInterceptor userContextInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userContextInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/auth/api/login", "/auth/api/register", "/auth/api/forgot-password", "/users/mail/api/sendOtp");

        // 注册金额格式化拦截器
        registry.addInterceptor(new AmountFormatInterceptor())
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要拦截的路径（可选）
                .excludePathPatterns("/static/**", "/error");
    }
}
