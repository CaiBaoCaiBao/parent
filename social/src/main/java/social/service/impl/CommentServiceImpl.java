package social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.client.ContentClient;
import common.client.UsersClient;
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
import social.pojo.dto.comment.CreateCommentDTO;
import social.pojo.dto.comment.DeleteCommentDTO;
import social.pojo.dto.comment.QueryCommentDTO;
import social.pojo.entity.Comment;
import social.pojo.entity.Like;
import social.pojo.vo.CommentVO;
import social.service.CommentService;
import users.pojo.vo.UserInfoVo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private LikeMapper likeMapper;

    @Autowired
    private UsersClient usersClient;

    @Autowired
    private ContentClient contentClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> createComment(CreateCommentDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();

        // 验证用户状态
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建评论操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 验证目标类型
        if (!Objects.equals(dto.getTargetType(), "travel_note") &&
            !Objects.equals(dto.getTargetType(), "destination") &&
            !Objects.equals(dto.getTargetType(), "attraction")) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "评论目标类型无效");
        }

        // 检查目标是否存在且状态有效
        try {
            if (Objects.equals(dto.getTargetType(), "travel_note")) {
                // 检查游记状态
                Result<?> travelNoteResult = contentClient.getTravelNoteDetail(dto.getTargetId());
                if (travelNoteResult == null || !travelNoteResult.getSuccess() || travelNoteResult.getData() == null) {
                    return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
                }
                Map<String, Object> travelNoteData = (Map<String, Object>) travelNoteResult.getData();
                Integer travelNoteStatus = (Integer) travelNoteData.get("status");
                // 只有已发布的游记才能评论
                if (!DictConstants.TravelNoteStatus.PUBLISHED.equals(travelNoteStatus)) {
                    return Result.error(ResultCode.FORBIDDEN.getCode(), "只能评论已发布的游记");
                }
            } else if (Objects.equals(dto.getTargetType(), "destination")) {
                // 检查目的地状态
                Result<?> destinationResult = contentClient.getDestinationDetail(dto.getTargetId());
                if (destinationResult == null || !destinationResult.getSuccess() || destinationResult.getData() == null) {
                    return Result.error(ResultCode.NOT_FOUND.getCode(), "目的地不存在");
                }
                Map<String, Object> destinationData = (Map<String, Object>) destinationResult.getData();
                String destinationStatus = (String) destinationData.get("status");
                // 只有已启用的目的地才能评论
                if (!"1".equals(destinationStatus)) {
                    return Result.error(ResultCode.FORBIDDEN.getCode(), "只能评论已启用的目的地");
                }
            } else if (Objects.equals(dto.getTargetType(), "attraction")) {
                // 检查景点状态
                Result<?> attractionResult = contentClient.getAttractionDetail(dto.getTargetId());
                if (attractionResult == null || !attractionResult.getSuccess() || attractionResult.getData() == null) {
                    return Result.error(ResultCode.NOT_FOUND.getCode(), "景点不存在");
                }
                Map<String, Object> attractionData = (Map<String, Object>) attractionResult.getData();
                Object attractionStatusObj = attractionData.get("status");
                Integer attractionStatus = attractionStatusObj instanceof Integer ? (Integer) attractionStatusObj :
                                          attractionStatusObj instanceof String ? Integer.valueOf((String) attractionStatusObj) : null;
                // 只有已启用的景点才能评论
                if (!Integer.valueOf(1).equals(attractionStatus)) {
                    return Result.error(ResultCode.FORBIDDEN.getCode(), "只能评论已启用的景点");
                }
            }
        } catch (Exception e) {
            log.error("检查目标状态失败: targetType={}, targetId={}", dto.getTargetType(), dto.getTargetId(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "检查目标状态失败");
        }

        // 如果是回复评论，验证父评论是否存在
        if (StringUtils.hasText(dto.getParentCommentId())) {
            QueryWrapper<Comment> parentWrapper = new QueryWrapper<>();
            parentWrapper.eq("comment_id", dto.getParentCommentId());
            Comment parentComment = commentMapper.selectOne(parentWrapper);
            if (parentComment == null) {
                return Result.error(ResultCode.NOT_FOUND.getCode(), "父评论不存在");
            }
        }

        // 创建评论
        Comment comment = new Comment();
        // 生成评论ID（使用 "COMMENT_" + ULID 格式，使ID更有语义）
        comment.setCommentId("COMMENT_" + common.utils.ULIDUtils.generateULID());
        comment.setUserId(currentUid);
        comment.setTargetType(dto.getTargetType());
        comment.setTargetId(dto.getTargetId());
        comment.setParentCommentId(dto.getParentCommentId());
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);

        return Result.success("评论成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteComment(DeleteCommentDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();

        // 验证用户状态
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除评论操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        List<String> commentIds = dto.getCommentIds();
        int deletedCount = 0;

        for (String commentId : commentIds) {
            // 查询评论
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("comment_id", commentId);
            Comment comment = commentMapper.selectOne(queryWrapper);

            if (comment == null) {
                log.warn("评论不存在: {}", commentId);
                continue;
            }

            // 验证权限：只能删除自己的评论，或者管理员可以删除任何评论
            if (!Objects.equals(comment.getUserId(), currentUid) && !Objects.equals(role, DictConstants.UserRole.ADMIN)) {
                log.warn("用户 {} 没有权限删除评论 {}", currentUid, commentId);
                continue;
            }

            // 删除评论（使用 comment_id 删除）
            QueryWrapper<Comment> deleteWrapper = new QueryWrapper<>();
            deleteWrapper.eq("comment_id", commentId);
            commentMapper.delete(deleteWrapper);
            deletedCount++;
        }

        return Result.success("成功删除 " + deletedCount + " 条评论");
    }

    @Override
    public Result<?> queryCommentList(QueryCommentDTO dto) {
        String currentUid = UserContext.getUserUUid();

        // 构建查询条件
        QueryWrapper<Comment> allQueryWrapper = new QueryWrapper<>();

        // 如果指定了目标类型且不是"全部"，则按目标类型过滤
        if (StringUtils.hasText(dto.getTargetType()) && !"all".equals(dto.getTargetType())) {
            allQueryWrapper.eq("target_type", dto.getTargetType());
        }

        // 如果指定了 targetId，则按 targetId 过滤
        if (StringUtils.hasText(dto.getTargetId())) {
            allQueryWrapper.eq("target_id", dto.getTargetId());
        }

        allQueryWrapper.orderByDesc("created_at");
        List<Comment> allComments = commentMapper.selectList(allQueryWrapper);

        // 查询所有父评论ID（去重）
        Set<String> parentCommentIds = allComments.stream()
                .map(Comment::getParentCommentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 查询这些父评论是否仍然存在（未被删除）
        Set<String> existingParentIds = new HashSet<>();
        if (!parentCommentIds.isEmpty()) {
            QueryWrapper<Comment> parentWrapper = new QueryWrapper<>();
            parentWrapper.in("comment_id", parentCommentIds);
            List<Comment> parentComments = commentMapper.selectList(parentWrapper);
            // 收集存在的父评论ID
            for (Comment parent : parentComments) {
                existingParentIds.add(parent.getCommentId());
            }
        }

        // 过滤掉父评论已被删除的子评论
        List<Comment> filteredComments = allComments.stream()
                .filter(comment -> {
                    // 顶级评论，保留
                    if (comment.getParentCommentId() == null) {
                        return true;
                    }
                    // 子评论，只有父评论存在时才保留
                    return existingParentIds.contains(comment.getParentCommentId());
                })
                .collect(Collectors.toList());

        // 如果指定了关键词，进行关键词搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            String keyword = dto.getKeyword().toLowerCase();
            
            // 收集所有用户ID
            List<String> userIds = filteredComments.stream()
                    .map(Comment::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            // 批量获取用户信息
            Map<String, UserInfoVo> userInfoMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                Result<?> batchResult = usersClient.getBatchUserInfo(userIds);
                if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                    List<?> rawData = (List<?>) batchResult.getData();
                    for (Object item : rawData) {
                        if (item instanceof UserInfoVo) {
                            UserInfoVo userInfo = (UserInfoVo) item;
                            userInfoMap.put(userInfo.getUUid(), userInfo);
                        } else if (item instanceof Map) {
                            Map<?, ?> map = (Map<?, ?>) item;
                            UserInfoVo userInfo = new UserInfoVo();
                            userInfo.setUUid((String) map.get("uuid"));
                            userInfo.setUserName((String) map.get("userName"));
                            userInfo.setNickName((String) map.get("nickName"));
                            userInfo.setAvatar((String) map.get("avatar"));
                            userInfoMap.put(userInfo.getUUid(), userInfo);
                        }
                    }
                }
            }

            // 按关键词过滤
            filteredComments = filteredComments.stream()
                    .filter(comment -> {
                        // 搜索评论内容
                        if (StringUtils.hasText(comment.getContent()) && 
                            comment.getContent().toLowerCase().contains(keyword)) {
                            return true;
                        }
                        
                        // 搜索用户昵称
                        UserInfoVo userInfo = userInfoMap.get(comment.getUserId());
                        if (userInfo != null) {
                            if (StringUtils.hasText(userInfo.getNickName()) && 
                                userInfo.getNickName().toLowerCase().contains(keyword)) {
                                return true;
                            }
                            if (StringUtils.hasText(userInfo.getUserName()) && 
                                userInfo.getUserName().toLowerCase().contains(keyword)) {
                                return true;
                            }
                        }
                        
                        return false;
                    })
                    .collect(Collectors.toList());
        }

        // 手动分页
        int total = filteredComments.size();
        int fromIndex = (dto.getPage() - 1) * dto.getPageSize();
        int toIndex = Math.min(fromIndex + dto.getPageSize(), total);
        List<Comment> pagedComments = filteredComments.subList(fromIndex, toIndex);

        // 收集所有用户ID
        List<String> userIds = pagedComments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<String, UserInfoVo> userInfoMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            Result<?> batchResult = usersClient.getBatchUserInfo(userIds);
            if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                // Feign 返回的数据可能是 LinkedHashMap，需要转换为 UserInfoVo
                List<?> rawData = (List<?>) batchResult.getData();
                log.info("批量获取用户信息，userIds: {}, 返回数据量: {}", userIds, rawData.size());
                for (Object item : rawData) {
                    if (item instanceof UserInfoVo) {
                        UserInfoVo userInfo = (UserInfoVo) item;
                        log.info("UserInfoVo对象: uUid={}, userName={}, nickName={}", userInfo.getUUid(), userInfo.getUserName(), userInfo.getNickName());
                        userInfoMap.put(userInfo.getUUid(), userInfo);
                    } else if (item instanceof Map) {
                        // 如果是 Map，手动转换为 UserInfoVo
                        Map<?, ?> map = (Map<?, ?>) item;
                        log.info("Map对象: {}", map);
                        UserInfoVo userInfo = new UserInfoVo();
                        // 注意：Map中的字段名是 uuid（全小写），不是 uUid
                        userInfo.setUUid((String) map.get("uuid"));
                        userInfo.setUserName((String) map.get("userName"));
                        userInfo.setNickName((String) map.get("nickName"));
                        userInfo.setAvatar((String) map.get("avatar"));
                        log.info("转换后的UserInfoVo: uUid={}, userName={}, nickName={}", userInfo.getUUid(), userInfo.getUserName(), userInfo.getNickName());
                        userInfoMap.put(userInfo.getUUid(), userInfo);
                    }
                }
                log.info("userInfoMap构建完成，包含的用户ID: {}", userInfoMap.keySet());
            } else {
                log.warn("批量获取用户信息失败: batchResult={}, success={}, data={}", batchResult, batchResult != null ? batchResult.getSuccess() : null, batchResult != null ? batchResult.getData() : null);
            }
        }

        // 转换为VO
        List<CommentVO> voList = pagedComments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getCommentId());
            vo.setUserId(comment.getUserId());
            vo.setTargetType(comment.getTargetType());
            vo.setTargetId(comment.getTargetId());
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setCreateTime(comment.getCreatedAt());
            vo.setUpdateTime(comment.getUpdatedAt());

            // 查询当前用户是否点赞
            if (currentUid != null) {
                QueryWrapper<Like> likeWrapper = new QueryWrapper<>();
                likeWrapper.eq("user_id", currentUid)
                        .eq("target_type", "comment")
                        .eq("target_id", comment.getCommentId());
                Like like = likeMapper.selectOne(likeWrapper);
                vo.setIsLiked(like != null);
            } else {
                vo.setIsLiked(false);
            }

            // 从用户服务获取用户信息（用户名、昵称、头像）
            UserInfoVo userInfo = userInfoMap.get(comment.getUserId());
            log.info("评论ID: {}, 用户ID: {}, userInfoMap中的keys: {}, 找到的userInfo: {}", comment.getCommentId(), comment.getUserId(), userInfoMap.keySet(), userInfo);
            if (userInfo != null) {
                vo.setUsername(userInfo.getUserName());
                vo.setNickname(userInfo.getNickName());
                vo.setAvatar(userInfo.getAvatar());
            } else {
                vo.setUsername("用户" + comment.getUserId().substring(0, 8));
                vo.setNickname("用户" + comment.getUserId().substring(0, 8));
                vo.setAvatar("");
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", (long) total);
        result.put("page", dto.getPage());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<?> getCommentCount() {
        // 查询评论总数（内部服务调用，无需权限验证）
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        Long commentCount = commentMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("commentCount", commentCount);

        return Result.success(result);
    }

    @Override
    public Result<?> getMonthlyCommentCount() {
        // 获取本月第一天和最后一天
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfMonth = now.withDayOfMonth(1);
        LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        // 查询本月新增评论数（内部服务调用，无需权限验证）
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", firstDayOfMonth.atStartOfDay());
        wrapper.le("created_at", lastDayOfMonth.atTime(23, 59, 59));
        Long monthlyCommentCount = commentMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyCommentCount", monthlyCommentCount);

        return Result.success(result);
    }

    @Override
    public Result<?> getLastMonthlyCommentCount() {
        // 获取上月第一天和最后一天
        LocalDate now = LocalDate.now();
        LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastDayOfLastMonth = now.withDayOfMonth(1).minusDays(1);

        // 查询上月新增评论数（内部服务调用，无需权限验证）
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", firstDayOfLastMonth.atStartOfDay());
        wrapper.le("created_at", lastDayOfLastMonth.atTime(23, 59, 59));
        Long lastMonthlyCommentCount = commentMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("lastMonthlyCommentCount", lastMonthlyCommentCount);

        return Result.success(result);
    }

    @Override
    public Result<?> getMonthlyCommentCountByMonth(int year, int month) {
        // 获取指定月份的第一天和最后一天
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

        // 查询指定月份的新增评论数（内部服务调用，无需权限验证）
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.ge("created_at", firstDayOfMonth.atStartOfDay());
        wrapper.le("created_at", lastDayOfMonth.atTime(23, 59, 59));
        Long monthlyCommentCount = commentMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("monthlyCommentCount", monthlyCommentCount);

        return Result.success(result);
    }

    @Override
    public Result<Integer> getCommentCountByTargetId(String targetId) {
        if (!StringUtils.hasText(targetId)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标ID不能为空");
        }

        // 查询指定目标的所有评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", "travel_note")
                .eq("target_id", targetId);
        List<Comment> allComments = commentMapper.selectList(queryWrapper);

        // 查询所有父评论ID（去重）
        Set<String> parentCommentIds = allComments.stream()
                .map(Comment::getParentCommentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 查询这些父评论是否仍然存在（未被删除）
        Set<String> existingParentIds = new HashSet<>();
        if (!parentCommentIds.isEmpty()) {
            QueryWrapper<Comment> parentWrapper = new QueryWrapper<>();
            parentWrapper.in("comment_id", parentCommentIds);
            List<Comment> parentComments = commentMapper.selectList(parentWrapper);
            // 收集存在的父评论ID
            for (Comment parent : parentComments) {
                existingParentIds.add(parent.getCommentId());
            }
        }

        // 过滤掉父评论已被删除的子评论，并计算总数
        long count = allComments.stream()
                .filter(comment -> {
                    // 顶级评论，保留
                    if (comment.getParentCommentId() == null) {
                        return true;
                    }
                    // 子评论，只有父评论存在时才保留
                    return existingParentIds.contains(comment.getParentCommentId());
                })
                .count();

        return Result.success((int) count);
    }

    @Override
    public Result<java.util.Map<String, Integer>> getBatchCommentCount(java.util.List<String> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Result.success(new java.util.HashMap<>());
        }

        // 批量查询所有评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", "travel_note")
                .in("target_id", targetIds);
        java.util.List<Comment> commentList = commentMapper.selectList(queryWrapper);

        // 查询所有父评论ID（去重）
        Set<String> parentCommentIds = commentList.stream()
                .map(Comment::getParentCommentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 查询这些父评论是否仍然存在（未被删除）
        Set<String> existingParentIds = new HashSet<>();
        if (!parentCommentIds.isEmpty()) {
            QueryWrapper<Comment> parentWrapper = new QueryWrapper<>();
            parentWrapper.in("comment_id", parentCommentIds);
            List<Comment> parentComments = commentMapper.selectList(parentWrapper);
            // 收集存在的父评论ID
            for (Comment parent : parentComments) {
                existingParentIds.add(parent.getCommentId());
            }
        }

        // 过滤掉父评论已被删除的子评论，并统计每个目标的评论数
        java.util.Map<String, Integer> countMap = new java.util.HashMap<>();
        for (String targetId : targetIds) {
            countMap.put(targetId, 0);
        }

        for (Comment comment : commentList) {
            // 过滤掉父评论已被删除的子评论
            if (comment.getParentCommentId() != null && !existingParentIds.contains(comment.getParentCommentId())) {
                continue;
            }
            String targetId = comment.getTargetId();
            countMap.put(targetId, countMap.getOrDefault(targetId, 0) + 1);
        }

        return Result.success(countMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteCommentsByTargetIds(String targetType, List<String> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标ID列表不能为空");
        }

        if (!StringUtils.hasText(targetType)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标类型不能为空");
        }

        try {
            // 删除指定目标类型和目标ID列表的所有评论
            QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("target_type", targetType);
            queryWrapper.in("target_id", targetIds);

            int deletedCount = commentMapper.delete(queryWrapper);
            log.info("批量删除评论成功: targetType={}, targetIds={}, deletedCount={}", targetType, targetIds, deletedCount);

            return Result.success("删除成功，共删除" + deletedCount + "条评论");
        } catch (Exception e) {
            log.error("批量删除评论失败: targetType={}, targetIds={}", targetType, targetIds, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除评论失败");
        }
    }
}
