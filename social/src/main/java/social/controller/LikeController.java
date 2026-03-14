package social.controller;

import common.utils.Result;
import social.pojo.dto.like.CheckLikeStatusDTO;
import social.pojo.dto.like.GetLikeCountDTO;
import social.pojo.dto.like.ToggleLikeDTO;
import social.pojo.vo.LikeStatusVO;
import social.service.LikeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
}
