package social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.mapper.LikeMapper;
import social.pojo.dto.like.CheckLikeStatusDTO;
import social.pojo.dto.like.GetLikeCountDTO;
import social.pojo.dto.like.ToggleLikeDTO;
import social.pojo.entity.Like;
import social.pojo.vo.LikeStatusVO;
import social.service.LikeService;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> toggleLike(ToggleLikeDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();

        // 验证用户状态
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("点赞操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 验证目标类型
        if (!Objects.equals(dto.getTargetType(), "travel_note") && !Objects.equals(dto.getTargetType(), "comment")) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "点赞目标类型无效");
        }

        // 查询是否已点赞
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid)
                .eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Like existingLike = likeMapper.selectOne(queryWrapper);

        if (existingLike != null) {
            // 已点赞，执行取消点赞
            likeMapper.deleteById(existingLike.getId());
            return Result.success("取消点赞成功");
        } else {
            // 未点赞，执行点赞
            Like like = new Like();
            // 生成点赞ID（使用 "LIKE_" + ULID 格式，使ID更有语义）
            like.setLikeId("LIKE_" + common.utils.ULIDUtils.generateULID());
            like.setUserId(currentUid);
            like.setTargetType(dto.getTargetType());
            like.setTargetId(dto.getTargetId());
            like.setCreatedAt(LocalDateTime.now());
            like.setUpdatedAt(LocalDateTime.now());
            likeMapper.insert(like);
            return Result.success("点赞成功");
        }
    }

    @Override
    public Result<LikeStatusVO> checkLikeStatus(CheckLikeStatusDTO dto) {
        String currentUid = UserContext.getUserUUid();

        // 查询点赞状态
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid)
                .eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Like existingLike = likeMapper.selectOne(queryWrapper);

        // 查询点赞总数
        QueryWrapper<Like> countWrapper = new QueryWrapper<>();
        countWrapper.eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Long likeCount = likeMapper.selectCount(countWrapper);

        LikeStatusVO vo = new LikeStatusVO();
        vo.setIsLiked(existingLike != null);
        vo.setLikeCount(likeCount.intValue());

        return Result.success(vo);
    }

    @Override
    public Result<Integer> getLikeCount(GetLikeCountDTO dto) {
        // 查询点赞总数
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Long likeCount = likeMapper.selectCount(queryWrapper);

        return Result.success(likeCount.intValue());
    }
}
