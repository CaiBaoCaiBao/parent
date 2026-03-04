package common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Component
public class BaseResponseAdvice implements ResponseBodyAdvice<Object> {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        // 如果返回类型已经是 Result 或者标注了忽略注解，则不处理
        return !returnType.getParameterType().equals(Result.class);
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {

        if (body == null) {
            return Result.success( "操作成功", null);
        }

        // String类型的返回值需要特殊处理，否则会产生类型转换异常 (ClassCastException)
        if (body instanceof String) {
            return objectMapper.writeValueAsString(Result.success( "操作成功", body));
        }

        // 全局异常处理返回的结果已经是Result了，这里不再重复包裹
        if (body instanceof Result) {
            return body;
        }

        return Result.success("操作成功", body);
    }
}
