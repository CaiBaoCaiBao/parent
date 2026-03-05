package common.common;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description(value = "刷新Token载荷")
public class RefreshPayload {
    private String uUid;
    private String nickName;
}
