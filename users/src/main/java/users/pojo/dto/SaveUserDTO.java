package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;

@Data
@Description("更新用户信息DTO")
public class SaveUserDTO {
    private String nickName;
    private String avatar;
    private String bio;
    private String phone;
    private LocalDate birthday;
}
