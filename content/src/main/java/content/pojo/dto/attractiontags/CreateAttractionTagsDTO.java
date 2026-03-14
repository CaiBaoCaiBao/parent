package content.pojo.dto.attractiontags;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("创建景点标签关联DTO")
public class CreateAttractionTagsDTO {

    @NotBlank(message = "景点ID不能为空")
    private String attractionId; // 景点ID

    @NotNull(message = "标签ID不能为空")
    private Long tagId;          // 标签ID

    private Integer weight;      // 权重

    private Boolean recommendFlag; // 是否推荐
}
