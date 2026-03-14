package content.pojo.dto.attraction;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("获取景点详情DTO")
public class GetAttractionDetailDTO {

    @NotBlank(message = "景点ID不能为空")
    private String aid; // 景点ID
}
