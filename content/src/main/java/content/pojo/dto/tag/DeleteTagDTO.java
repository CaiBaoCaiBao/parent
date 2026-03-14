package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除标签DTO")
public class DeleteTagDTO {
    private List<String> tagIds;
}
