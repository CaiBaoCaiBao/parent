package content.pojo.dto.attractiontags;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询景点标签关联DTO")
public class QueryAttractionTagsDTO {

    private String attractionId; // 景点ID
    private Long tagId;          // 标签ID
    private Boolean recommendFlag; // 是否推荐
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
}
