package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("更新用户状态DTO")
public class UpdateUserStatusDTO {
    private String uUid;
    private String status;
}
