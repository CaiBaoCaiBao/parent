package content.pojo.dto.opentimerule;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除开放时间规则DTO")
public class DeleteOpenTimeRuleDTO {

    @NotEmpty(message = "开放时间规则ID列表不能为空")
    private List<String> otrIds;  // 开放时间规则ID列表
}
