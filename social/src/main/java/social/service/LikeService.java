package social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import social.pojo.dto.like.CheckLikeStatusDTO;
import social.pojo.dto.like.GetLikeCountDTO;
import social.pojo.dto.like.QueryLikeListDTO;
import social.pojo.dto.like.ToggleLikeDTO;
import social.pojo.entity.Like;
import social.pojo.vo.LikeStatusVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.Map;

@Description("点赞管理")
public interface LikeService extends IService<Like> {
    @Description("点赞/取消点赞")
    Result<?> toggleLike(@Valid ToggleLikeDTO dto);

    @Description("检查点赞状态")
    Result<LikeStatusVO> checkLikeStatus(@Valid CheckLikeStatusDTO dto);

    @Description("获取点赞数")
    Result<Integer> getLikeCount(@Valid GetLikeCountDTO dto);

    @Description("批量获取点赞数")
    Result<Map<String, Integer>> getBatchLikeCount(List<String> targetIds);

    @Description("查询点赞列表")
    Result<?> queryLikeList(@Valid QueryLikeListDTO dto);

    @Description("根据目标类型和目标ID列表批量删除点赞")
    Result<?> deleteLikesByTargetIds(String targetType, List<String> targetIds);
}
