package content.pojo.dto.destination;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("更新目的地DTO")
public class UpdateDestinationDTO {

    @NotNull(message = "ID不能为空")
    private String destinationId;              // 目的地ID

    @NotBlank(message = "目的地名称不能为空")
    private String name;           // 目的地名称

    private String aliasesName;   // 别名
    private String destCode;      // 目的地代码
    private String longitude;     // 经度
    private String latitude;      // 纬度
    private String coverImg;      // 封面图片
    private String description;   // 简述
    private String content;       // 详细内容
    private String province;      // 省份
    private String city;          // 城市
    private Integer level;        // 层级：1-城市，2-景区
    private String bestSeason;    // 最佳季节
    private Integer travelDays;   // 建议游玩天数
    private Integer status;        // 状态：0-禁用，1-启用
    private Integer sortOrder;     // 排序
}
