package users.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Description("用户信息VO")
public class UserInfoVo {
    private String uUid;
    private String userName;
    private String nickName;
    private String avatar;
    private String role;
    private String status;
    private String bio;
    private String phone;
    private LocalDate birthday;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
