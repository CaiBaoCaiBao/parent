package common.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        contextId= ClientInfo.ContentService.SERVICE_CONTEXT_ID,
        name= ClientInfo.ContentService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface ContentClient {

    @GetMapping("/travel-note/api/list")
    Result<?> queryTravelNoteList(@RequestParam("userId") String userId,
                                   @RequestParam("pageNum") Integer pageNum,
                                   @RequestParam("pageSize") Integer pageSize);

    @GetMapping("/travel-note/api/detail")
    Result<?> getTravelNoteDetail(@RequestParam("noteId") String noteId);

    @GetMapping("/travel-note/api/batch-detail")
    Result<?> getBatchTravelNoteDetail(@RequestParam("noteIds") List<String> noteIds);

    @GetMapping("/destination/api/detail")
    Result<?> getDestinationDetail(@RequestParam("destinationId") String destinationId);

    @GetMapping("/destination/api/batch-detail")
    Result<?> getBatchDestinationDetail(@RequestParam("destinationIds") List<String> destinationIds);

    @GetMapping("/attraction/api/detail")
    Result<?> getAttractionDetail(@RequestParam("aid") String aid);

    @GetMapping("/attraction/api/batch-detail")
    Result<?> getBatchAttractionDetail(@RequestParam("attractionIds") List<String> attractionIds);
}
