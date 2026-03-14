package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
@Data
@TableName("tags")
@Description("标签类型")
public class Tags {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String tid;
    private String tagName;
    private String tagCode;
    private String tagTypeId;
    private String iconUrl;
    private String color;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
