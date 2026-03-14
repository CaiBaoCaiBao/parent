package common.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        contextId= ClientInfo.UsersService.SERVICE_CONTEXT_ID,
        name= ClientInfo.UsersService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface UsersClient {

    @GetMapping("/user/api/info")
    Result<?> getUserInfo(@RequestParam("userName") String userName);

    @GetMapping("/user/api/batch-info")
    Result<?> getBatchUserInfo(@RequestParam("uids") List<String> uids);

    @GetMapping("/user/api/count")
    Result<?> getUserCount();
}
