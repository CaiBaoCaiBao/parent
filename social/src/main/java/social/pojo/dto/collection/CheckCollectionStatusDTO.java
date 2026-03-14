package social.pojo.dto.collection;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("检查收藏状态DTO")
public class CheckCollectionStatusDTO {

    @NotBlank(message = "收藏目标类型不能为空")
    private String targetType;    // 收藏目标类型：travel_note-游记

    @NotBlank(message = "收藏目标ID不能为空")
    private String targetId;      // 收藏目标ID
}
