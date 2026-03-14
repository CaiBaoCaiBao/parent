package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("创建标签DTO")
public class CreateTagDTO {
    private String tagName;
    private String tagCode;
    private String tagTypeId;
    private String iconUrl;
    private String color;
    private Integer status;
    private Integer sortOrder;
}
