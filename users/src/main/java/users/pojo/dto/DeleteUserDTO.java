package users.pojo.dto;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;
@Data
@Description("删除用户DTO")
public class DeleteUserDTO {
    private List<String> uuids;
}
