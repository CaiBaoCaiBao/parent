package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
@Data
@TableName("attraction_tags")
@Description("景点-标签")
public class AttractionTags {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long attractionId;
    private Long tagId;
    private Integer weight;
    private Boolean recommendFlag;
    private LocalDateTime createdAt;
}
