package social.pojo.dto.collection;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询收藏列表DTO")
public class QueryCollectionListDTO {

    private String targetType;    // 收藏目标类型：travel_note-游记，destination-目的地，attraction-景点（可选）

    private Integer page = 1;     // 页码
    private Integer pageSize = 10; // 每页数量
}
