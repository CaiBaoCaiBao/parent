package content.pojo.dto.openhoursexception;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;

@Data
@Description("更新例外日期DTO")
public class UpdateOpenHoursExceptionDTO {

    @NotBlank(message = "例外日期ID不能为空")
    private String oheId;         // 例外日期ID

    private LocalDate exceptionDate; // 例外日期
    private Boolean closedFlag;   // 是否全天关闭
    private TimeSlotDTO timeSlot; // 自定义时间段
    private Integer exceptionType; // 例外类型
    private String description;   // 描述

    @Data
    public static class TimeSlotDTO {
        private String start;  // 开始时间
        private String end;    // 结束时间
    }
}
