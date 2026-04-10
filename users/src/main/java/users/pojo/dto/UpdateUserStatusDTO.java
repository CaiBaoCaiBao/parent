package users.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("更新用户状态DTO")
public class UpdateUserStatusDTO {
    @NotBlank(message = "用户UUID不能为空")
    private String uuid;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^(active|inactive)$", message = "状态值必须是 active 或 inactive")
    private String status;
}
