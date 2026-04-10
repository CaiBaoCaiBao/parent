package social.pojo.dto.comment;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("审核评论DTO")
public class AuditCommentDTO {

    @NotEmpty(message = "评论ID不能为空")
    private String commentId;     // 评论ID

    @NotEmpty(message = "审核状态不能为空")
    private String status;        // 审核状态：approved-通过，rejected-拒绝

    private String reason;        // 拒绝原因（可选）
}
