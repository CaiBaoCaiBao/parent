package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
@Data
@TableName("destination_tags")
@Description("目的地-标签")
public class DestinationTags {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long destinationId;
    private Long tagId;
    private Integer weight;
    private Boolean recommendFlag;
    private LocalDateTime createdAt;
}
