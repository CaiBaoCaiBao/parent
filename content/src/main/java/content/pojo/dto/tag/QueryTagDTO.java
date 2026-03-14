package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询标签DTO")
public class QueryTagDTO {
    private String tagTypeId;
    private String tagName;
    private String tagCode;
    private Integer status;
}
