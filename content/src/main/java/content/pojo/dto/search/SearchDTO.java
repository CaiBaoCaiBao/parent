package content.pojo.dto.search;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SearchDTO {

    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 搜索类型：travel_note（游记）、destination（目的地）、all（全部）
     */
    private String type = "all";

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}
