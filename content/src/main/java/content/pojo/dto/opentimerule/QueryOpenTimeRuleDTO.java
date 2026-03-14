package content.pojo.dto.opentimerule;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询开放时间规则DTO")
public class QueryOpenTimeRuleDTO {

    private String otrId;        // 开放时间规则ID
    private String attractionId; // 景点ID
    private String scheduleType; // 调度类型
    private Integer status;      // 状态
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
}
