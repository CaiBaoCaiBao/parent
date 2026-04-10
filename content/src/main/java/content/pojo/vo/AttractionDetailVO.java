package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Description("景点详情VO")
public class AttractionDetailVO {
    private Long id;
    private String aid;
    private String destinationId;
    private String destinationName;
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

    // 关联信息
    private List<TagVO> tags; // 标签列表
    private List<TicketVO> tickets; // 门票列表
    private List<PlayItemVO> playItems; // 游玩项目列表
    private List<OpenTimeRuleVO> openTimeRules; // 开放时间规则列表

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
    public static class TicketVO {
        private Long id;
        private String tid;
        private String ticketName;
        private String ticketCode;
        private String ticketType;
        private BigDecimal price;
        private BigDecimal discountPrice;
        private Integer stock;
        private Integer validDays;
        private String description;
        private Integer sortOrder;
    }

    @Data
    public static class PlayItemVO {
        private Long id;
        private String aid;
        private String name;
        private List<String> images;
        private String description;
        private Integer duration;
        private Integer maxPerson;
        private Integer minPerson;
        private Integer minAge;
        private Integer maxAge;
        private BigDecimal price;
        private BigDecimal discountPrice;
    }

    @Data
    public static class OpenTimeRuleVO {
        private Long id;
        private String otrId;
        private String openTimeName;
        private String scheduleType;
        private Integer priority;
        private String startDate;
        private String endDate;
        private String dayOfWeek;
        private List<TimeSlotVO> timeSlots;
        private String description;
        private Boolean holidayFollowFlag;
    }

    @Data
    public static class TimeSlotVO {
        private LocalTime start;
        private LocalTime end;
    }
}
