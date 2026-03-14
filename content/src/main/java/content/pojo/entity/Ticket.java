package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import common.interfaces.AmountFormat;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ticket")
@Description("门票")
public class Ticket {
    private Long id;
    private String tid;
    private String ticketName;
    private String ticketCode;
    private String attractionId;
    private String playItemId;
    /**
     * 成人票/儿童票/学生票/老人票
     */
    private String ticketType;
    @AmountFormat
    private BigDecimal price;
    @AmountFormat
    private BigDecimal discountPrice;
    /**
     * -1 不限量
     */
    private Integer stock;
    /**
     * 有效期
     */
    private Integer validDays;
    private String description;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
