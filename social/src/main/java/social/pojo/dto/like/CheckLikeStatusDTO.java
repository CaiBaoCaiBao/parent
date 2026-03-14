package social.pojo.dto.like;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("检查点赞状态DTO")
public class CheckLikeStatusDTO {

    @NotBlank(message = "点赞目标类型不能为空")
    private String targetType;    // 点赞目标类型：travel_note-游记，comment-评论

    @NotBlank(message = "点赞目标ID不能为空")
    private String targetId;      // 点赞目标ID
}
