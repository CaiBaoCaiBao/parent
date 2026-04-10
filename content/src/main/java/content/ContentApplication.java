package content;

import common.config.MybatisConfig;
import common.interfaces.EnableUnifiedResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@EnableUnifiedResponse
@SpringBootApplication
@EnableFeignClients(basePackages = {"content", "common"})
@ComponentScan(basePackages = {"content", "common"})
@Import(MybatisConfig.class)
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
