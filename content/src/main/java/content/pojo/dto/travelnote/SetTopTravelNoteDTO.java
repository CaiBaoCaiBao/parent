package content.pojo.dto.travelnote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("置顶游记DTO")
public class SetTopTravelNoteDTO {

    @NotBlank(message = "游记ID不能为空")
    private String noteId;

    @NotNull(message = "是否置顶不能为空")
    private Boolean isTop;
}
