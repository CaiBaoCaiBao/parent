package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@TableName("travel_note")
@Description("游记")
public class TravelNote {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String noteId;        // 游记ID
    private String userId;        // 用户ID
    @TableField(exist = false)
    private String userName;      // 用户名（非数据库字段，用于查询时返回）
    @TableField(exist = false)
    private String nickName;      // 昵称（非数据库字段，用于查询时返回）
    private String destinationId; // 目的地ID
    private String title;         // 游记标题
    private String coverImg;     // 封面图片
    private String images;        // 图片列表（JSON格式）
    private String content;      // 游记内容
    private Integer travelDays;   // 游玩天数
    private Double budget;        // 预算
    private Integer viewCount;    // 浏览次数
    private Integer likeCount;    // 点赞次数
    private Integer commentCount; // 评论次数
    private Integer status;       // 状态
    private Integer sortOrder;    // 排序
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
