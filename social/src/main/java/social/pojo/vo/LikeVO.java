package social.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("点赞VO")
public class LikeVO {
    private String likeId;       // 点赞ID
    private String userId;       // 用户ID
    private String targetType;   // 点赞目标类型
    private String targetId;     // 点赞目标ID
    private String targetTitle;  // 目标标题（如游记标题）
    private String targetCover;  // 目标封面（如游记封面）
    private String targetContent;// 目标内容摘要
    private String authorId;     // 作者ID
    private String authorName;   // 作者昵称
    private String authorUserName; // 作者用户名
    private String authorAvatar; // 作者头像
    private Integer viewCount;   // 浏览数
    private Integer commentCount;// 评论数
    private Integer likeCount;   // 点赞数
    private Integer collectionCount; // 收藏数
    private LocalDateTime createdAt; // 创建时间
}
