package content.pojo.dto.opentimerule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;
import java.util.List;

@Data
@Description("创建开放时间规则DTO")
public class CreateOpenTimeRuleDTO {

    @NotBlank(message = "景点ID不能为空")
    private String attractionId;  // 景点ID

    private String openTimeName;  // 开放时间名称

    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;  // 调度类型：fixed/seasonal/weekly/holiday

    private Integer priority;     // 优先级，0最高

    private LocalDate startDate;  // 生效开始日期（季节性规则用）

    private LocalDate endDate;    // 生效结束日期（季节性规则用）

    private String dayOfWeek;     // 周几适用（1-营业，0-不营业，顺序：周日,周一,周二,周三,周四,周五,周六）

    @NotNull(message = "时间段不能为空")
    private List<TimeSlotDTO> timeSlots; // 营业时间段

    private String description;   // 描述

    private Boolean holidayFollowFlag; // 是否跟随节日放假

    @Data
    public static class TimeSlotDTO {
        private String start;  // 开始时间，格式：09:00
        private String end;    // 结束时间，格式：12:00
    }
}
