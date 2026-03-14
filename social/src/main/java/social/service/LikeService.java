package social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import social.pojo.dto.like.CheckLikeStatusDTO;
import social.pojo.dto.like.GetLikeCountDTO;
import social.pojo.dto.like.ToggleLikeDTO;
import social.pojo.entity.Like;
import social.pojo.vo.LikeStatusVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("点赞管理")
public interface LikeService extends IService<Like> {
    @Description("点赞/取消点赞")
    Result<?> toggleLike(@Valid ToggleLikeDTO dto);

    @Description("检查点赞状态")
    Result<LikeStatusVO> checkLikeStatus(@Valid CheckLikeStatusDTO dto);

    @Description("获取点赞数")
    Result<Integer> getLikeCount(@Valid GetLikeCountDTO dto);
}
