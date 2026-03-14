package content.pojo.dto.openhoursexception;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除例外日期DTO")
public class DeleteOpenHoursExceptionDTO {

    @NotEmpty(message = "例外日期ID列表不能为空")
    private List<String> oheIds;  // 例外日期ID列表
}
