package content.pojo.dto.ticket;

import common.interfaces.AmountFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;

@Data
@Description("创建门票DTO")
public class CreateTicketDTO {

    @NotBlank(message = "门票名称不能为空")
    private String ticketName;    // 门票名称

    private String ticketCode;    // 门票代码

    @NotBlank(message = "景点ID不能为空")
    private String attractionId;  // 景点ID

    private String playItemId;    // 游玩项目ID

    @NotBlank(message = "门票类型不能为空")
    private String ticketType;    // 门票类型：成人票/儿童票/学生票/老人票

    @NotNull(message = "价格不能为空")
    @AmountFormat(scale = 2)
    private BigDecimal price;      // 价格

    @AmountFormat(scale = 2)
    private BigDecimal discountPrice; // 折扣价格

    private Integer stock;         // 库存，-1表示不限量

    private Integer validDays;     // 有效期（天）

    private String description;    // 描述

    private Integer sortOrder;     // 排序

    private Integer status;        // 状态：1-启用，0-禁用
}
