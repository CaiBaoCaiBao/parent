package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Description("目的地详情VO")
public class DestinationDetailVO {
    private Long id;
    private String destinationId;
    private String name;
    private String aliasesName;
    private String destCode;
    private BigDecimal longitude;
    private BigDecimal latitude;
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

    // 关联信息
    private List<TagVO> tags; // 标签列表
    private List<AttractionSimpleVO> attractions; // 景点列表

    @Data
    public static class TagVO {
        private Long id;
        private String tid;
        private String tagName;
        private String tagCode;
        private String iconUrl;
        private String color;
        private Integer weight;
        private Boolean recommendFlag;
    }

    @Data
    public static class AttractionSimpleVO {
        private Long id;
        private String aid;
        private String name;
        private String coverImg;
        private String description;
        private Integer viewCount;
        private Integer sortOrder;
    }
}
