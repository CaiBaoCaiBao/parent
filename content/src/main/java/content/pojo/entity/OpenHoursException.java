package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("open_hours_exception")
@Description("例外日期表")
public class OpenHoursException {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String oheId;
    private String attractionId;
    private LocalDate exceptionDate;
    /**
     * 是否全天关闭
     */
    private Boolean closedFlag;
    /**
     * 自定义时间段
     */
    private TimeSlot timeSlot;
    /**
     * 例外类型
     * 1-节假日，2-淡季调整，3-临时关闭，4-特殊活动
     */
    private Integer exceptionType;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
