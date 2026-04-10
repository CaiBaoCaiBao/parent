package social.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@TableName("like_record")
@Description("点赞记录")
public class Like {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String likeId;        // 点赞ID
    private String userId;        // 用户ID
    private String targetType;    // 点赞目标类型：travel_note-游记，comment-评论
    private String targetId;      // 点赞目标ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
