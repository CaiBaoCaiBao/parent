package content.pojo.dto.openhoursexception;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;

@Data
@Description("创建例外日期DTO")
public class CreateOpenHoursExceptionDTO {

    @NotBlank(message = "景点ID不能为空")
    private String attractionId;  // 景点ID

    @NotNull(message = "例外日期不能为空")
    private LocalDate exceptionDate; // 例外日期

    private Boolean closedFlag;   // 是否全天关闭

    private TimeSlotDTO timeSlot; // 自定义时间段

    @NotNull(message = "例外类型不能为空")
    private Integer exceptionType; // 例外类型：1-节假日，2-淡季调整，3-临时关闭，4-特殊活动

    private String description;   // 描述

    @Data
    public static class TimeSlotDTO {
        private String start;  // 开始时间
        private String end;    // 结束时间
    }
}
