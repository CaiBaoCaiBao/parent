package content.pojo.dto.destination;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.context.annotation.Description;

/**
 * 查询热门目的地DTO
 */
@Data
@Description("查询热门目的地DTO")
public class QueryHotDestinationDTO {

    /**
     * 返回数量，默认10条
     */
    @Min(value = 1, message = "返回数量至少为1")
    @Max(value = 100, message = "返回数量最多为100")
    private Integer limit = 10;
}
