package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import common.interfaces.AmountFormat;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("attraction")
@Description("景点")
public class Attraction {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String aid;
    private String destinationId;
    private String name;
    private List<String> images;
    private String address;
    private String phone;
    /**
     * 经纬度后期替换成 GEOGRAPHY
     */
    @AmountFormat(scale = 7)
    private BigDecimal longitude; // 经度
    @AmountFormat(scale = 7)
    private BigDecimal latitude; // 维度
    private String description;
    private Integer viewCount;
    private Integer status;
    private Integer sortOrder;
    private Boolean realTimeSyncFlag; // 是否实时同步，默认不同步
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
