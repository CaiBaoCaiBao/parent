package content.pojo.dto.search;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 保存搜索记录DTO
 */
@Data
public class SaveSearchLogDTO {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    @Size(max = 100, message = "搜索关键词长度不能超过100个字符")
    private String keyword;

    /**
     * 搜索类型：all/destination/travel_note/attraction
     */
    private String searchType = "all";

    /**
     * 搜索结果数量
     */
    private Integer resultCount = 0;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;
}
