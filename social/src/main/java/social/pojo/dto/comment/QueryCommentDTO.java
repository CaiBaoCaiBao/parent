package social.pojo.dto.comment;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询评论列表DTO")
public class QueryCommentDTO {

    private String targetType;    // 评论目标类型：travel_note-游记, destination-目的地, attraction-景点, all-全部

    private String targetId;      // 评论目标ID（可选，为空时查询所有）

    private String keyword;       // 关键词（搜索评论内容、用户昵称、用户名）

    private Integer page = 1;     // 页码
    private Integer pageSize = 10; // 每页数量
}
