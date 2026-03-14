package social.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("评论VO")
public class CommentVO {
    private String commentId;      // 评论ID
    private String userId;        // 用户ID
    private String username;      // 用户名
    private String nickname;      // 昵称
    private String avatar;        // 头像
    private String targetType;    // 评论目标类型
    private String targetId;      // 评论目标ID
    private String parentCommentId; // 父评论ID
    private String content;       // 评论内容
    private Integer likeCount;    // 点赞数
    private Boolean isLiked;      // 当前用户是否点赞
    private LocalDateTime createdAt; // 创建时间
}
