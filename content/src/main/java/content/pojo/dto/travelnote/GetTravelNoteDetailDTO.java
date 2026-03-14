package content.pojo.dto.travelnote;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("获取游记详情DTO")
public class GetTravelNoteDetailDTO {

    @NotBlank(message = "游记ID不能为空")
    private String noteId; // 游记ID
}
