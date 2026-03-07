package users.pojo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("创建管理员DTO")
public class CreateAdminDTO {
    @NotBlank(message = "用户名不能为空")
    private String userName;
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank(message = "角色不能为空")
    private String role;
}
