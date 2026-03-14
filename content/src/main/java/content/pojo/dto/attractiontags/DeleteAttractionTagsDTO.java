package content.pojo.dto.attractiontags;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除景点标签关联DTO")
public class DeleteAttractionTagsDTO {

    @NotEmpty(message = "关联ID列表不能为空")
    private List<Long> ids;  // 关联ID列表
}
