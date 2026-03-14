package content.pojo.dto.travelnote;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

@Data
@Description("更新游记DTO")
public class UpdateTravelNoteDTO {

    @NotBlank(message = "游记ID不能为空")
    private String noteId;        // 游记ID

    private String title;         // 游记标题
    private String destinationId; // 目的地ID
    private List<String> attractionIds; // 景点ID列表
    private String coverImg;      // 封面图片
    private List<String> images;  // 图片列表
    private String content;       // 游记内容
    private Integer travelDays;   // 游玩天数
    private Double budget;        // 预算
    private Integer sortOrder;    // 排序
}
