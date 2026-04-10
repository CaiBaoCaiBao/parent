package social.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@TableName("comment")
@Description("评论")
public class Comment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String commentId;      // 评论ID
    private String userId;        // 用户ID
    private String targetType;    // 评论目标类型：travel_note-游记，destination-目的地，attraction-景点
    private String targetId;      // 评论目标ID
    private String parentCommentId; // 父评论ID（用于回复）
    private String content;       // 评论内容
    private Integer likeCount;    // 点赞数
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
