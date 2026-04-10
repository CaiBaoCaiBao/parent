package social.pojo.dto.comment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除评论DTO")
public class DeleteCommentDTO {

    @NotEmpty(message = "评论ID列表不能为空")
    @JsonProperty("commentIds")
    private List<String> commentIds;    // 评论ID列表（支持批量删除）
}
