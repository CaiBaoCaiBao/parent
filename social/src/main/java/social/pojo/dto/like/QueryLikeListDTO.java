package social.pojo.dto.like;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class QueryLikeListDTO {
    private Integer page = 1;

    private Integer pageSize = 10;

    private String userId;

    private String targetType;

    private String targetId;
}
