package common.config;

import common.utils.FeignDecodeResult;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @FeignClient(name = "user-service", configuration = FeignConfig.class)
 * public interface UserClient {
 *     // 就像调用本地 Service 一样，直接返回 User
 *     @GetMapping("/users/{id}")
 *     User getUser(@PathVariable("id") Long id);
 * }
 */

@Configuration
public class FeignConfig {
    @Autowired
    private ObjectFactory<HttpMessageConverters> messageConverters;

    @Bean
    public Decoder feignDecoder() {
        // 使用 SpringDecoder 配合自定义逻辑
        return new FeignDecodeResult(new SpringDecoder(messageConverters));
    }

    @Bean
    @Primary
    public Encoder feignEncoder() {
        // 使用 SpringFormEncoder 支持 multipart/form-data 文件上传
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }
}