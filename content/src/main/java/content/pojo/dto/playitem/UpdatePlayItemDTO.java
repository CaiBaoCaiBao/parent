package content.pojo.dto.playitem;

import common.interfaces.AmountFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.util.List;

@Data
@Description("更新游玩项目DTO")
public class UpdatePlayItemDTO {

    @NotBlank(message = "游玩项目ID不能为空")
    private String aid;           // 游玩项目ID

    private String name;          // 游玩项目名称
    private String attractionId;  // 景点ID
    private List<String> images;  // 图片列表
    private String description;   // 描述
    private Integer duration;     // 时长
    private Integer maxPerson;    // 最大人数
    private Integer minPerson;    // 最小人数
    private Integer minAge;       // 最小年龄
    private Integer maxAge;       // 最大年龄
    @AmountFormat(scale = 2)
    private BigDecimal price;     // 价格
    @AmountFormat(scale = 2)
    private BigDecimal discountPrice; // 折扣价格
}
