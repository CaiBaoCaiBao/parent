package common.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        contextId= ClientInfo.SocialService.SERVICE_CONTEXT_ID,
        name= ClientInfo.SocialService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface SocialClient {

    @GetMapping("/comment/api/count")
    Result<?> getCommentCount();
}
