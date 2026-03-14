package content.pojo.dto.destinationtags;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询目的地标签关联DTO")
public class QueryDestinationTagsDTO {

    private String destinationId; // 目的地ID
    private Long tagId;          // 标签ID
    private Boolean recommendFlag; // 是否推荐
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
}
