package content.pojo.dto.attraction;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除景点DTO")
public class DeleteAttractionDTO {

    @NotEmpty(message = "景点ID列表不能为空")
    private List<String> aids;  // 景点ID列表
}
