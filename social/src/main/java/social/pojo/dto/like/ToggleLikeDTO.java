package social.pojo.dto.like;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("点赞/取消点赞DTO")
public class ToggleLikeDTO {

    @NotBlank(message = "点赞目标类型不能为空")
    private String targetType;    // 点赞目标类型：travel_note-游记，comment-评论

    @NotBlank(message = "点赞目标ID不能为空")
    private String targetId;      // 点赞目标ID
}
