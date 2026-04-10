package content.pojo.dto.playitem;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除游玩项目DTO")
public class DeletePlayItemDTO {

    @NotEmpty(message = "游玩项目ID列表不能为空")
    private List<String> piids;  // 游玩项目ID列表
}
