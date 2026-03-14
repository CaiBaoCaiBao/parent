package content.pojo.dto.banner;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 删除轮播图DTO
 */
@Data
public class DeleteBannerDTO {

    /**
     * 轮播图ID列表
     */
    @NotEmpty(message = "轮播图ID列表不能为空")
    private List<Long> ids;
}
