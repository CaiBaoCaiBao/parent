package social.pojo.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("删除评论DTO")
public class DeleteCommentDTO {

    @NotBlank(message = "评论ID不能为空")
    private String commentId;    // 评论ID
}
