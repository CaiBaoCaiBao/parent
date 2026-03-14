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

@Data
@TableName("destination")
@Description("目的地表")
public class Destination {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String destinationId;
    private String name;
    private String aliasesName; // 别名
    private String destCode;
    /**
     * 经纬度后期替换成 GEOGRAPHY
     */
    @AmountFormat(scale = 7)
    private BigDecimal longitude; // 经度
    @AmountFormat(scale = 7)
    private BigDecimal latitude; // 维度
    private String coverImg;
    private String description;
    private String province; // 省份
    private String city; // 城市
    private Integer level; // 1-城市，2-景区
    private String bestSeason;
    private Integer travelDays;
    private Integer viewCount;
    private String status;
    private String sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}