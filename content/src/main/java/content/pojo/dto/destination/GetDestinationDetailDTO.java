package content.pojo.dto.destination;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("获取目的地详情DTO")
public class GetDestinationDetailDTO {

    @NotBlank(message = "目的地ID不能为空")
    private String destinationId; // 目的地ID
}
