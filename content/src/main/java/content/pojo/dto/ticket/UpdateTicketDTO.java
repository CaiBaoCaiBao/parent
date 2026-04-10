package content.pojo.dto.ticket;

import common.interfaces.AmountFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;

@Data
@Description("更新门票DTO")
public class UpdateTicketDTO {

    @NotBlank(message = "门票ID不能为空")
    private String tid;           // 门票ID

    private String ticketName;    // 门票名称
    private String ticketCode;    // 门票代码
    private String attractionId;  // 景点ID
    private String playItemId;    // 游玩项目ID
    private String ticketType;    // 门票类型
    @AmountFormat(scale = 2)
    private BigDecimal price;      // 价格
    @AmountFormat(scale = 2)
    private BigDecimal discountPrice; // 折扣价格
    private Integer stock;         // 库存
    private Integer validDays;     // 有效期
    private String description;    // 描述
    private Integer sortOrder;     // 排序
    private Integer status;        // 状态：1-启用，0-禁用
}
