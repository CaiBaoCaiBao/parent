package users.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@Description("用户列表VO")
public class UserListVo {
    private String uUid;
    private String userName;
    private String email;
    private String nickName;
    private String role;
    private String status;
    private String avatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
