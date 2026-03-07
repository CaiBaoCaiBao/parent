package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("查询用户列表DTO")
public class QueryUserListDTO {
    private String name;
    private String status;
    private String role;
    private String email;
}
