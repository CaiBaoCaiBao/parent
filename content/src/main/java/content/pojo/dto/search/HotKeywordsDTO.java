package content.pojo.dto.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 热门搜索DTO
 */
@Data
public class HotKeywordsDTO {

    /**
     * 返回数量限制
     */
    @Min(value = 1, message = "返回数量至少为1")
    @Max(value = 20, message = "返回数量最多为20")
    private Integer limit = 10;
}
