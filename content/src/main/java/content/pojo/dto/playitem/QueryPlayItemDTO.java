package content.pojo.dto.playitem;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询游玩项目DTO")
public class QueryPlayItemDTO {

    private String aid;           // 游玩项目ID
    private String attractionId;  // 景点ID
    private String name;          // 名称（模糊查询）
    private Integer status;       // 状态
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
}
