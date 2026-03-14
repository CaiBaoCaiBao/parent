package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@TableName("tag_type")
@Description("标签类型")
public class TagType {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String tagTypeId;
    private String tagTypeName;
    private String tagTypeCode; // 标签代码,也是英文名
    private String iconUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
