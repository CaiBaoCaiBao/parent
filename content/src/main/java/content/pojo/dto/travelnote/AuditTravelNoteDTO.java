package content.pojo.dto.travelnote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("审核游记DTO")
public class AuditTravelNoteDTO {

    @NotNull(message = "游记ID列表不能为空")
    private List<String> noteIds;

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    private String rejectReason;
}
