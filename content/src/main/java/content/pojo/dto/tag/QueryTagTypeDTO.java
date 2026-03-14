package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询标签类型DTO")
public class QueryTagTypeDTO {
    private String tagTypeName;
    private String tagTypeCode;
    private Integer status;
}
