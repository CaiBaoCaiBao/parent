package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("更新标签DTO")
public class UpdateTagDTO {
    private String tagId;
    private String tagName;
    private String tagCode;
    private String iconUrl;
    private String color;
    private Integer status;
    private Integer sortOrder;
}
