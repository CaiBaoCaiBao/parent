package social.pojo.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("创建评论DTO")
public class CreateCommentDTO {

    @NotBlank(message = "评论目标类型不能为空")
    private String targetType;    // 评论目标类型：travel_note-游记，destination-目的地，attraction-景点

    @NotBlank(message = "评论目标ID不能为空")
    private String targetId;      // 评论目标ID

    private String parentCommentId; // 父评论ID（用于回复，可选）

    @NotBlank(message = "评论内容不能为空")
    private String content;       // 评论内容
}
