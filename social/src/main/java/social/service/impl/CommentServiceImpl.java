package social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
        if (!Objects.equals(dto.getTargetType(), "travel_note")) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "评论目标类型无效");
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

        // 查询评论
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", dto.getCommentId());
        Comment comment = commentMapper.selectOne(queryWrapper);

        if (comment == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "评论不存在");
        }

        // 验证权限：只能删除自己的评论，或者管理员可以删除任何评论
        if (!Objects.equals(comment.getUserId(), currentUid) && !Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }

        // 删除评论
        commentMapper.deleteById(comment.getId());

        return Result.success("删除评论成功");
    }

    @Override
    public Result<?> queryCommentList(QueryCommentDTO dto) {
        String currentUid = UserContext.getUserUUid();

        // 构建查询条件
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId())
                .orderByDesc("created_at");

        // 分页查询
        Page<Comment> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Comment> commentPage = commentMapper.selectPage(page, queryWrapper);

        // 收集所有用户ID
        List<String> userIds = commentPage.getRecords().stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<String, Map<String, Object>> userInfoMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            Result<?> batchResult = usersClient.getBatchUserInfo(userIds);
            if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                List<Map<String, Object>> userInfoList = (List<Map<String, Object>>) batchResult.getData();
                for (Map<String, Object> userInfo : userInfoList) {
                    String uUid = (String) userInfo.get("uUid");
                    userInfoMap.put(uUid, userInfo);
                }
            }
        }

        // 转换为VO
        List<CommentVO> voList = commentPage.getRecords().stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setCommentId(comment.getCommentId());
            vo.setUserId(comment.getUserId());
            vo.setTargetType(comment.getTargetType());
            vo.setTargetId(comment.getTargetId());
            vo.setParentCommentId(comment.getParentCommentId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setCreatedAt(comment.getCreatedAt());

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
            Map<String, Object> userInfo = userInfoMap.get(comment.getUserId());
            if (userInfo != null) {
                vo.setUsername((String) userInfo.get("userName"));
                vo.setNickname((String) userInfo.get("nickName"));
                vo.setAvatar((String) userInfo.get("avatar"));
            } else {
                vo.setUsername("用户" + comment.getUserId().substring(0, 8));
                vo.setNickname("用户" + comment.getUserId().substring(0, 8));
                vo.setAvatar("");
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", commentPage.getTotal());
        result.put("page", dto.getPage());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<?> getCommentCount() {
        // 获取当前用户信息
        String currentRole = UserContext.getRole();
        String currentStatus = UserContext.getStatus();

        // 只有管理员才能查看评论总数
        if (!DictConstants.UserRole.ADMIN.equals(currentRole)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "只有管理员才能查看评论总数");
        }

        // 检查当前用户状态
        if (!Objects.equals(currentStatus, DictConstants.UserStatus.ACTIVE)) {
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账号已被禁用，无法执行此操作");
        }

        // 查询评论总数
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        Long commentCount = commentMapper.selectCount(wrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("commentCount", commentCount);

        return Result.success(result);
    }
}
