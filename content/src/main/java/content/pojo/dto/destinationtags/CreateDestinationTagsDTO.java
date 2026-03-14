package content.pojo.dto.destinationtags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("创建目的地标签关联DTO")
public class CreateDestinationTagsDTO {

    @NotBlank(message = "目的地ID不能为空")
    private String destinationId; // 目的地ID

    @NotNull(message = "标签ID不能为空")
    private Long tagId;          // 标签ID

    private Integer weight;      // 权重

    private Boolean recommendFlag; // 是否推荐
}
