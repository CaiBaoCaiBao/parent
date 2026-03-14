package content.pojo.dto.tag;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("删除标签类型DTO")
public class DeleteTagTypeDTO {
    private List<String> tagTypeIds;
}
