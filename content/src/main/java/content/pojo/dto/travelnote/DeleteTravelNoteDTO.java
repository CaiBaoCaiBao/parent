package content.pojo.dto.travelnote;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除游记DTO")
public class DeleteTravelNoteDTO {

    @NotEmpty(message = "游记ID列表不能为空")
    private List<String> noteIds; // 游记ID列表
}
