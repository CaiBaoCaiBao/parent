package content.pojo.dto.banner;

import lombok.Data;

/**
 * 查询轮播图列表DTO
 */
@Data
public class QueryBannerDTO {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 标题（模糊查询）
     */
    private String title;

    /**
     * 链接类型：0-无，1-游记，2-目的地
     */
    private Integer linkType;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;
}
