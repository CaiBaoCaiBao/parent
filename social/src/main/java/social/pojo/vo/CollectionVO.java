package social.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("收藏VO")
public class CollectionVO {
    private String collectionId;  // 收藏ID
    private String userId;        // 用户ID
    private String targetType;    // 收藏目标类型
    private String targetId;      // 收藏目标ID
    private String targetTitle;   // 目标标题（如游记标题）
    private String targetCover;   // 目标封面（如游记封面）
    private LocalDateTime createdAt; // 创建时间
}
