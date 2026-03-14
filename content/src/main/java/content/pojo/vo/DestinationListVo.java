package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("目的地列表")
public class DestinationListVo {
    private String destinationId;
    private String name;
    private String aliasesName; // 别名
    private String coverImg;
    private String description; // 描述
    private String province; // 省份
    private String city; // 城市
    private Integer level; // 1-城市，2-景区
    private String bestSeason; // 最佳季节
    private Integer travelDays;
    private Integer viewCount;
    private String status;
    private String sortOrder;
}
