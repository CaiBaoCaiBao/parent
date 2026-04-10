package common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;

/**
 * Jackson 配置类
 * 用于配置 JSON 序列化和反序列化行为
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // 注册自定义模块，将 BigDecimal 序列化为字符串，避免科学计数法
        SimpleModule module = new SimpleModule();
        module.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        objectMapper.registerModule(module);

        // 禁用将 BigDecimal 写成科学计数法
        objectMapper.disable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

        return objectMapper;
    }
}
