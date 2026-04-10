package social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.client.ContentClient;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import social.mapper.CommentMapper;
import social.mapper.LikeMapper;
import social.pojo.dto.like.CheckLikeStatusDTO;
import social.pojo.dto.like.GetLikeCountDTO;
import social.pojo.dto.like.QueryLikeListDTO;
import social.pojo.dto.like.ToggleLikeDTO;
import social.pojo.entity.Comment;
import social.pojo.entity.Like;
import social.pojo.vo.LikeStatusVO;
import social.pojo.vo.LikeVO;
import social.service.LikeService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LikeServiceImpl extends ServiceImpl<LikeMapper, Like> implements LikeService {

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ContentClient contentClient;

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

        // 如果是游记，检查游记状态
        if ("travel_note".equals(dto.getTargetType())) {
            try {
                Result<?> travelNoteResult = contentClient.getTravelNoteDetail(dto.getTargetId());
                if (travelNoteResult == null || !travelNoteResult.getSuccess() || travelNoteResult.getData() == null) {
                    return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
                }
                Map<String, Object> travelNoteData = (Map<String, Object>) travelNoteResult.getData();
                Integer travelNoteStatus = (Integer) travelNoteData.get("status");
                // 只有已发布的游记才能点赞
                if (!DictConstants.TravelNoteStatus.PUBLISHED.equals(travelNoteStatus)) {
                    return Result.error(ResultCode.FORBIDDEN.getCode(), "只能点赞已发布的游记");
                }
            } catch (Exception e) {
                log.error("检查游记状态失败: targetId={}", dto.getTargetId(), e);
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "检查游记状态失败");
            }
        }

        // 查询是否已点赞
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid)
                .eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Like existingLike = likeMapper.selectOne(queryWrapper);

        if (existingLike != null) {
            // 已点赞，执行取消点赞（物理删除）
            likeMapper.deleteById(existingLike.getId());

            // 如果是评论，更新评论的点赞数
            if ("comment".equals(dto.getTargetType())) {
                updateCommentLikeCount(dto.getTargetId(), -1);
            }

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

            // 如果是评论，更新评论的点赞数
            if ("comment".equals(dto.getTargetType())) {
                updateCommentLikeCount(dto.getTargetId(), 1);
            }

            return Result.success("点赞成功");
        }
    }

    @Override
    public Result<LikeStatusVO> checkLikeStatus(CheckLikeStatusDTO dto) {
        String currentUid = UserContext.getUserUUid();
        log.info("检查点赞状态 - 用户ID: {}, 目标类型: {}, 目标ID: {}", currentUid, dto.getTargetType(), dto.getTargetId());

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

        log.info("点赞状态结果 - 是否点赞: {}, 点赞数: {}", vo.getIsLiked(), vo.getLikeCount());
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

    @Override
    public Result<java.util.Map<String, Integer>> getBatchLikeCount(java.util.List<String> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Result.success(new java.util.HashMap<>());
        }

        // 批量查询点赞数量
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", "travel_note")
                .in("target_id", targetIds);
        java.util.List<Like> likeList = likeMapper.selectList(queryWrapper);

        // 统计每个目标的点赞数
        java.util.Map<String, Integer> countMap = new java.util.HashMap<>();
        for (String targetId : targetIds) {
            countMap.put(targetId, 0);
        }

        for (Like like : likeList) {
            String targetId = like.getTargetId();
            countMap.put(targetId, countMap.getOrDefault(targetId, 0) + 1);
        }

        return Result.success(countMap);
    }

    @Override
    public Result<?> queryLikeList(QueryLikeListDTO dto) {
        log.info("查询点赞列表 - userId: {}, targetType: {}, page: {}, pageSize: {}",
                dto.getUserId(), dto.getTargetType(), dto.getPage(), dto.getPageSize());

        // 构建查询条件
        QueryWrapper<Like> queryWrapper = new QueryWrapper<>();

        if (dto.getUserId() != null && !dto.getUserId().isEmpty()) {
            queryWrapper.eq("user_id", dto.getUserId());
        }

        if (dto.getTargetType() != null && !dto.getTargetType().isEmpty()) {
            queryWrapper.eq("target_type", dto.getTargetType());
        }

        if (dto.getTargetId() != null && !dto.getTargetId().isEmpty()) {
            queryWrapper.eq("target_id", dto.getTargetId());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<Like> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Like> resultPage = likeMapper.selectPage(page, queryWrapper);

        log.info("分页查询结果 - 总记录数: {}, 当前页记录数: {}, 当前页: {}, 总页数: {}",
                resultPage.getTotal(), resultPage.getRecords().size(),
                resultPage.getCurrent(), resultPage.getPages());

        // 收集所有目标ID
        List<String> targetIds = resultPage.getRecords().stream()
                .map(Like::getTargetId)
                .collect(Collectors.toList());

        log.info("目标ID列表: {}", targetIds);

        // 批量获取游记详情
        Map<String, Map<String, Object>> travelNoteMap = new HashMap<>();
        if (!targetIds.isEmpty()) {
            try {
                log.info("开始调用内容服务获取游记详情，targetIds={}", targetIds);
                Result<?> batchResult = contentClient.getBatchTravelNoteDetail(targetIds);
                log.info("调用内容服务返回结果: success={}, code={}, message={}, data类型={}",
                        batchResult != null ? batchResult.getSuccess() : null,
                        batchResult != null ? batchResult.getCode() : null,
                        batchResult != null ? batchResult.getMessage() : null,
                        batchResult != null && batchResult.getData() != null ? batchResult.getData().getClass().getName() : null);

                if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                    Object data = batchResult.getData();
                    log.info("返回数据内容: {}", data);

                    // Feign 返回的是 List<Map<String, Object>> 格式
                    List<Map<String, Object>> travelNotes = null;
                    if (data instanceof List) {
                        travelNotes = (List<Map<String, Object>>) data;
                        log.info("数据是List类型，大小: {}", travelNotes.size());
                    } else if (data instanceof Map) {
                        Map<?, ?> dataMap = (Map<?, ?>) data;
                        log.info("数据是Map类型，keys: {}", dataMap.keySet());
                        Object records = dataMap.get("records");
                        if (records instanceof List) {
                            travelNotes = (List<Map<String, Object>>) records;
                            log.info("从Map中提取records，大小: {}", travelNotes.size());
                        }
                    }

                    if (travelNotes != null) {
                        log.info("开始处理游记列表，数量: {}", travelNotes.size());
                        for (Map<String, Object> note : travelNotes) {
                            String noteId = (String) note.get("noteId");
                            log.info("处理游记完整数据: noteId={}, 所有字段={}", noteId, note);
                            log.info("处理游记关键字段: noteId={}, nickName={}, avatar={}, userName={}",
                                    noteId, note.get("nickName"), note.get("avatar"), note.get("userName"));
                            if (noteId != null) {
                                travelNoteMap.put(noteId, note);
                            }
                        }
                    }
                    log.info("成功获取游记详情，数量: {}", travelNoteMap.size());
                } else {
                    log.warn("获取游记详情失败: result={}, success={}, data={}",
                            batchResult, batchResult != null ? batchResult.getSuccess() : null,
                            batchResult != null ? batchResult.getData() : null);
                }
            } catch (Exception e) {
                log.error("调用内容服务获取游记详情异常", e);
            }
        }

        // 转换为VO
        List<LikeVO> voList = resultPage.getRecords().stream().map(like -> {
            LikeVO vo = new LikeVO();
            vo.setLikeId(like.getLikeId());
            vo.setUserId(like.getUserId());
            vo.setTargetType(like.getTargetType());
            vo.setTargetId(like.getTargetId());
            vo.setCreatedAt(like.getCreatedAt());

            // 从内容服务获取目标信息
            Map<String, Object> travelNote = travelNoteMap.get(like.getTargetId());
            if (travelNote != null) {
                vo.setTargetTitle((String) travelNote.get("title"));
                vo.setTargetCover((String) travelNote.get("coverImg"));
                vo.setTargetContent((String) travelNote.get("content"));
                vo.setAuthorId((String) travelNote.get("userId"));
                vo.setAuthorName((String) travelNote.get("nickName"));
                vo.setAuthorUserName((String) travelNote.get("userName"));
                vo.setAuthorAvatar((String) travelNote.get("avatar"));
                vo.setViewCount(getIntegerValue(travelNote.get("viewCount")));
                vo.setCommentCount(getIntegerValue(travelNote.get("commentCount")));
                vo.setLikeCount(getIntegerValue(travelNote.get("likeCount")));
                vo.setCollectionCount(getIntegerValue(travelNote.get("collectionCount")));

                log.info("设置游记信息: targetId={}, authorName={}, authorUserName={}, authorAvatar={}",
                        like.getTargetId(), vo.getAuthorName(), vo.getAuthorUserName(), vo.getAuthorAvatar());
            } else {
                log.warn("游记详情不存在: targetId={}", like.getTargetId());
                vo.setTargetTitle("游记已删除");
                vo.setTargetCover("");
                vo.setTargetContent("");
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("records", voList);
        resultMap.put("total", resultPage.getTotal());
        resultMap.put("size", resultPage.getSize());
        resultMap.put("current", resultPage.getCurrent());

        return Result.success(resultMap);
    }

    /**
     * 安全获取整数值
     */
    private Integer getIntegerValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 更新评论的点赞数
     */
    private void updateCommentLikeCount(String commentId, int delta) {
        try {
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("comment_id", commentId);
            Comment comment = commentMapper.selectOne(queryWrapper);

            if (comment != null) {
                int newCount = (comment.getLikeCount() != null ? comment.getLikeCount() : 0) + delta;
                comment.setLikeCount(Math.max(0, newCount)); // 确保不为负数
                commentMapper.updateById(comment);
                log.info("更新评论点赞数: commentId={}, delta={}, newCount={}", commentId, delta, newCount);
            }
        } catch (Exception e) {
            log.error("更新评论点赞数失败: commentId={}", commentId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteLikesByTargetIds(String targetType, List<String> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标ID列表不能为空");
        }

        if (!StringUtils.hasText(targetType)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标类型不能为空");
        }

        try {
            // 删除指定目标类型和目标ID列表的所有点赞
            QueryWrapper<Like> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("target_type", targetType);
            queryWrapper.in("target_id", targetIds);

            int deletedCount = likeMapper.delete(queryWrapper);
            log.info("批量删除点赞成功: targetType={}, targetIds={}, deletedCount={}", targetType, targetIds, deletedCount);

            return Result.success("删除成功，共删除" + deletedCount + "条点赞记录");
        } catch (Exception e) {
            log.error("批量删除点赞失败: targetType={}, targetIds={}", targetType, targetIds, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除点赞失败");
        }
    }
}
