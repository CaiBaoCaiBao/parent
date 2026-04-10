package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import common.interfaces.AmountFormat;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "play_item", autoResultMap = true)
@Description("游玩项目")
public class PlayItem {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String piid;
    private String aid;
    private String name;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;
    private String description;
    private Integer duration;
    private Integer maxPerson;
    private Integer minPerson;
    private Integer minAge;
    private Integer maxAge;
    /**
     * 价格
     */
    @AmountFormat(scale = 2)
    private BigDecimal price;
    /**
     * 折扣价格
     */
    @AmountFormat(scale = 2)
    private BigDecimal discountPrice;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
