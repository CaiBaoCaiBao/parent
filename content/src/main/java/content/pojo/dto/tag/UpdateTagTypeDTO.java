package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("更新标签类型DTO")
public class UpdateTagTypeDTO {
    private String tegTypeId;
    private String tagTypeName;
    private String tagTypeCode;
    private String iconUrl;
    private Integer status;
}
