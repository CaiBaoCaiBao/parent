package common.common;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description(value = "认证Token载荷")
public class AccessPayload {
    private String role;
    private String email;
    private String uUid;
    private String userName; // @xxx
    private String avatar;
    private String status;
    private String nickName;
    private String jit;
}
