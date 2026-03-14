package content.pojo.dto.attraction;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询景点DTO")
public class QueryAttractionDTO {

    private String aid;           // 景点ID
    private String destinationId; // 目的地ID
    private String name;          // 景点名称（模糊查询）
    private Integer status;       // 状态
    private Integer pageNum = 1; // 页码
    private Integer pageSize = 10; // 每页数量
}
