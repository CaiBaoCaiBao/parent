package content.pojo.dto.playitem;

import common.interfaces.AmountFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.util.List;

@Data
@Description("创建游玩项目DTO")
public class CreatePlayItemDTO {

    @NotBlank(message = "游玩项目名称不能为空")
    private String name;          // 游玩项目名称

    @NotBlank(message = "景点ID不能为空")
    private String attractionId;  // 景点ID

    private List<String> images;  // 图片列表

    private String description;   // 描述

    private Integer duration;     // 时长（分钟）

    private Integer maxPerson;    // 最大人数

    private Integer minPerson;    // 最小人数

    private Integer minAge;       // 最小年龄

    private Integer maxAge;       // 最大年龄

    @NotNull(message = "价格不能为空")
    @AmountFormat(scale = 2)
    private BigDecimal price;     // 价格

    @AmountFormat(scale = 2)
    private BigDecimal discountPrice; // 折扣价格
}
