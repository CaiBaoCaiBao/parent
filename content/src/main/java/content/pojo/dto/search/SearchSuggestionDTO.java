package content.pojo.dto.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 搜索建议DTO
 */
@Data
public class SearchSuggestionDTO {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 返回数量限制
     */
    @Min(value = 1, message = "返回数量至少为1")
    @Max(value = 20, message = "返回数量最多为20")
    private Integer limit = 10;
}
