package content.pojo.dto.openhoursexception;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;

@Data
@Description("查询例外日期DTO")
public class QueryOpenHoursExceptionDTO {

    private String oheId;        // 例外日期ID
    private String attractionId; // 景点ID
    private LocalDate exceptionDate; // 例外日期
    private Integer exceptionType; // 例外类型
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页数量
}
