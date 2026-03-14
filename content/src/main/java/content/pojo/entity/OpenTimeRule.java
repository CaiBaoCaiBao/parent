package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("open_time_rule")
@Description("开放时间规则")
public class OpenTimeRule {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String otrId;
    private String attractionId;
    private String openTimeName;
    private String scheduleType; // 'fixed', 'seasonal', 'weekly', 'holiday'
    private Integer priority; // 优先级，0最高
    /**
     * 季节性规则用
     */
    private LocalDate startDate; // 生效开始日期
    private LocalDate endDate; // 生效结束日期
    /**
     * 周几适用（1-营业，0-不营业，顺序：周日,周一,周二,周三,周四,周五,周六）
     */
    private String dayOfWeek;
    /**
     * 营业时间段
     * 格式：[{"start": "09:00", "end": "12:00"}, {"start": "13:00", "end": "17:00"}]
     */
    private List<TimeSlot> timeSlots;
    private String description;
    /**
     * 是否跟随节日放假
     */
    private Boolean holidayFollowFlag;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
