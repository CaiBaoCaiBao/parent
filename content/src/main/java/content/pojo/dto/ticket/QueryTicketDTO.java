package content.pojo.dto.ticket;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询门票DTO")
public class QueryTicketDTO {

    private String tid;           // 门票ID
    private String attractionId;  // 景点ID
    private String playItemId;    // 游玩项目ID
    private String ticketType;    // 门票类型
    private Integer status;       // 状态
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
}
