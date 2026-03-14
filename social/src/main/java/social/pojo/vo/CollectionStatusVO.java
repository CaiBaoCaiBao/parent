package social.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Description;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Description("收藏状态VO")
public class CollectionStatusVO {
    private Boolean isCollected;  // 是否已收藏
    private Integer collectionCount; // 收藏总数
}
