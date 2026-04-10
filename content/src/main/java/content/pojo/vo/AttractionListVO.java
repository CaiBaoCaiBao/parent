package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Description("景点列表VO")
public class AttractionListVO {
    private Long id;
    private String aid;
    private String destinationId;
    private String name;
    private List<String> images;
    private String address;
    private String phone;
    // private BigDecimal longitude;
    // private BigDecimal latitude;
    private String description;
    private Integer viewCount;
    private Integer status;
    private Integer sortOrder;
    private Boolean realTimeSyncFlag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
