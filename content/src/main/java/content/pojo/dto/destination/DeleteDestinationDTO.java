package content.pojo.dto.destination;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除目的地DTO")
public class DeleteDestinationDTO {

    @NotNull(message = "ID不能为空")
    private List<String> destinationIds;  // 目的地ID
}
