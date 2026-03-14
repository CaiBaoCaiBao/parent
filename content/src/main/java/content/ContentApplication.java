package content;

import common.interfaces.EnableUnifiedResponse;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@EnableUnifiedResponse
@SpringBootApplication
@EnableFeignClients(basePackages = {"content", "common","users"})
@ComponentScan(basePackages = {"content", "common","users"})
public class ContentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentApplication.class, args);
    }

}
