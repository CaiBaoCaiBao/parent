package content.pojo.dto.destinationtags;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除目的地标签关联DTO")
public class DeleteDestinationTagsDTO {

    @NotEmpty(message = "关联ID列表不能为空")
    private List<Long> ids;  // 关联ID列表
}
