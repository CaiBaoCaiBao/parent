package social.controller;

import common.utils.Result;
import social.pojo.dto.like.CheckLikeStatusDTO;
import social.pojo.dto.like.GetLikeCountDTO;
import social.pojo.dto.like.QueryLikeListDTO;
import social.pojo.dto.like.ToggleLikeDTO;
import social.pojo.vo.LikeStatusVO;
import social.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/like")
public class LikeController {
    @Autowired
    LikeService likeService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/toggle")
    @Description(value = "点赞/取消点赞")
    public Result<?> toggleLike(@RequestBody @Valid ToggleLikeDTO dto) {
        return likeService.toggleLike(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/status")
    @Description(value = "检查点赞状态")
    public Result<LikeStatusVO> checkLikeStatus(@ModelAttribute @Valid CheckLikeStatusDTO dto) {
        return likeService.checkLikeStatus(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/count")
    @Description(value = "获取点赞数")
    public Result<Integer> getLikeCount(@ModelAttribute @Valid GetLikeCountDTO dto) {
        return likeService.getLikeCount(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/batch-count")
    @Description(value = "批量获取点赞数")
    public Result<Map<String, Integer>> getBatchLikeCount(@RequestParam("targetIds") List<String> targetIds) {
        return likeService.getBatchLikeCount(targetIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询点赞列表")
    public Result<?> queryLikeList(@ModelAttribute @Valid QueryLikeListDTO dto) {
        return likeService.queryLikeList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete-by-targets")
    @Description(value = "根据目标类型和目标ID列表批量删除点赞")
    public Result<?> deleteLikesByTargetIds(@RequestParam("targetType") String targetType, @RequestParam("targetIds") List<String> targetIds) {
        return likeService.deleteLikesByTargetIds(targetType, targetIds);
    }
}
