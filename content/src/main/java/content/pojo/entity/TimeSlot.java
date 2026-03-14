package content.pojo.entity;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalTime;

@Data
@Description("时间段")
public class TimeSlot {
    private LocalTime start;
    private LocalTime end;
}
