package social.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@TableName("collection")
@Description("收藏")
public class Collection {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String collectionId;  // 收藏ID
    private String userId;        // 用户ID
    private String targetType;    // 收藏目标类型：travel_note-游记，destination-目的地，attraction-景点
    private String targetId;      // 收藏目标ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
