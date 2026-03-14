package content.pojo.dto.opentimerule;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;
import java.util.List;

@Data
@Description("更新开放时间规则DTO")
public class UpdateOpenTimeRuleDTO {

    @NotBlank(message = "开放时间规则ID不能为空")
    private String otrId;         // 开放时间规则ID

    private String openTimeName;  // 开放时间名称
    private String scheduleType;  // 调度类型
    private Integer priority;     // 优先级
    private LocalDate startDate;  // 生效开始日期
    private LocalDate endDate;    // 生效结束日期
    private String dayOfWeek;     // 周几适用
    private List<TimeSlotDTO> timeSlots; // 营业时间段
    private String description;   // 描述
    private Boolean holidayFollowFlag; // 是否跟随节日放假

    @Data
    public static class TimeSlotDTO {
        private String start;  // 开始时间
        private String end;    // 结束时间
    }
}
