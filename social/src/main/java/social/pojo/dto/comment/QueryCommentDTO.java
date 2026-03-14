package social.pojo.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询评论列表DTO")
public class QueryCommentDTO {

    @NotBlank(message = "评论目标类型不能为空")
    private String targetType;    // 评论目标类型：travel_note-游记

    @NotBlank(message = "评论目标ID不能为空")
    private String targetId;      // 评论目标ID

    private Integer page = 1;     // 页码
    private Integer pageSize = 10; // 每页数量
}
