package content.pojo.dto.banner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建轮播图DTO
 */
@Data
public class CreateBannerDTO {

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 图片URL
     */
    @NotBlank(message = "图片URL不能为空")
    private String image;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 链接类型：0-无，1-游记，2-目的地
     */
    private Integer linkType;

    /**
     * 目标ID（游记ID或目的地ID）
     */
    private String targetId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-下架，1-上架
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
