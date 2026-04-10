package content.pojo.dto.travelnote;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询游记DTO")
public class QueryTravelNoteDTO {

    private String noteId;        // 游记ID
    private String userId;        // 用户ID
    private String destinationId; // 目的地ID
    private String title;         // 标题（模糊查询）
    private Integer status;       // 状态
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
    private Integer offset;       // 偏移量（用于分页）
}
