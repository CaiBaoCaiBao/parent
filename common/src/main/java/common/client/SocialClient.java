package common.client;

import common.config.FeignConfig;
import common.constants.ClientInfo;
import common.utils.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        contextId= ClientInfo.SocialService.SERVICE_CONTEXT_ID,
        name= ClientInfo.SocialService.SERVICE_NAME,
        configuration = FeignConfig.class
)
public interface SocialClient {

    @GetMapping("/comment/api/count")
    Result<?> getCommentCount();

    /**
     * 获取单个目标的评论数量
     */
    @GetMapping("/comment/api/count-by-target")
    Result<?> getCommentCount(@RequestParam("targetId") String targetId);

    /**
     * 获取本月新增评论数
     */
    @GetMapping("/comment/api/monthly-count")
    Result<?> getMonthlyCommentCount();

    /**
     * 获取上月新增评论数
     */
    @GetMapping("/comment/api/last-monthly-count")
    Result<?> getLastMonthlyCommentCount();

    /**
     * 获取指定月份的新增评论数
     */
    @GetMapping("/comment/api/monthly-count-by-month")
    Result<?> getMonthlyCommentCountByMonth(@RequestParam("year") int year, @RequestParam("month") int month);

    /**
     * 获取单个目标的点赞数量
     */
    @GetMapping("/like/api/count")
    Result<?> getLikeCount(@RequestParam("targetType") String targetType, @RequestParam("targetId") String targetId);

    /**
     * 批量获取点赞数量
     */
    @GetMapping("/like/api/batch-count")
    Result<?> getBatchLikeCount(@RequestParam("targetIds") List<String> targetIds);

    /**
     * 批量获取收藏数量
     */
    @GetMapping("/collection/api/batch-count")
    Result<?> getBatchCollectionCount(@RequestParam("targetIds") List<String> targetIds);

    /**
     * 获取单个目标的收藏数量
     */
    @GetMapping("/collection/api/count")
    Result<?> getCollectionCount(@RequestParam("targetId") String targetId);

    /**
     * 批量获取评论数量
     */
    @GetMapping("/comment/api/batch-count")
    Result<?> getBatchCommentCount(@RequestParam("targetIds") List<String> targetIds);

    /**
     * 根据目标类型和目标ID列表批量删除评论
     */
    @DeleteMapping("/comment/api/delete-by-targets")
    Result<?> deleteCommentsByTargetIds(@RequestParam("targetType") String targetType, @RequestParam("targetIds") List<String> targetIds);

    /**
     * 根据目标类型和目标ID列表批量删除点赞
     */
    @DeleteMapping("/like/api/delete-by-targets")
    Result<?> deleteLikesByTargetIds(@RequestParam("targetType") String targetType, @RequestParam("targetIds") List<String> targetIds);

    /**
     * 根据目标类型和目标ID列表批量删除收藏
     */
    @DeleteMapping("/collection/api/delete-by-targets")
    Result<?> deleteCollectionsByTargetIds(@RequestParam("targetType") String targetType, @RequestParam("targetIds") List<String> targetIds);
}
