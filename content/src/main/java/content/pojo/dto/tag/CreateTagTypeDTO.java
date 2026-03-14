package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("创建标签类型")
public class CreateTagTypeDTO {
    private String tagTypeName;
    private String tagTypeCode;
    private String iconUrl;
    private Integer status;
}
