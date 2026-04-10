package content.pojo.dto.attraction;

import com.fasterxml.jackson.annotation.JsonProperty;
import common.interfaces.AmountFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.util.List;

@Data
@Description("更新景点DTO")
public class UpdateAttractionDTO {

    @NotBlank(message = "景点ID不能为空")
    @JsonProperty("aid")
    private String aid;           // 景点ID

    private String name;          // 景点名称
    private String destinationId; // 所属目的地ID
    private List<String> images;  // 景点图片列表
    private String address;       // 地址
    private String phone;         // 联系电话
    // @AmountFormat(scale = 7)
    // private BigDecimal longitude; // 经度
    // @AmountFormat(scale = 7)
    // private BigDecimal latitude;  // 纬度
    private String description;   // 景点描述
    private Integer sortOrder;   // 排序
    private Boolean realTimeSyncFlag; // 是否实时同步
    private Integer status;       // 状态：0-禁用，1-启用
}
