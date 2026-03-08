package file.config;

import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

@Data
@Configuration
@ConfigurationProperties(prefix = "oss")
public class OSSConfig {
    @Value("${oss.access-key}")
    private String AK;
    @Value("${oss.secret-key}")
    private String SK;
    @Value("${oss.domain}")
    private String domain;
    @Value("${oss.bucket}")
    private String bucketName;

    @Bean
    public Auth auth(){
        return Auth.create(AK,SK);
    }

    @Description("获取上传token")
    public String getUploadToken(){
        return auth().uploadToken(bucketName);
    }
}
