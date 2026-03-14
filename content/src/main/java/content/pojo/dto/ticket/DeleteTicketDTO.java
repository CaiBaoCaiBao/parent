package content.pojo.dto.ticket;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除门票DTO")
public class DeleteTicketDTO {

    @NotEmpty(message = "门票ID列表不能为空")
    private List<String> tids;  // 门票ID列表
}
