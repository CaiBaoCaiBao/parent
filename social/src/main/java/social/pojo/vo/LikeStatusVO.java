package social.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Description;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("点赞状态VO")
public class LikeStatusVO {
    private Boolean isLiked;      // 是否已点赞
    private Integer likeCount;    // 点赞总数
}
