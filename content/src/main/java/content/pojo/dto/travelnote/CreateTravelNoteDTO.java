package content.pojo.dto.travelnote;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("创建游记DTO")
public class CreateTravelNoteDTO {

    @NotBlank(message = "游记标题不能为空")
    private String title;         // 游记标题

    @NotBlank(message = "用户ID不能为空")
    private String userId;        // 用户ID

    @NotBlank(message = "目的地ID不能为空")
    private String destinationId; // 目的地ID

    private List<String> attractionIds; // 景点ID列表
    private String coverImg;     // 封面图片
    private List<String> images; // 图片列表
    private String content;      // 游记内容
    private Integer travelDays;  // 游玩天数
    private Double budget;       // 预算
    private Integer sortOrder;   // 排序
}
