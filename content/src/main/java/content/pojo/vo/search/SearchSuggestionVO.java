package content.pojo.vo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索建议VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionVO {

    /**
     * 建议关键词
     */
    private String keyword;

    /**
     * 建议类型：destination（目的地）、attraction（景点）、travel_note（游记）
     */
    private String type;

    /**
     * 关联ID（可选）
     */
    private String id;

    /**
     * 额外信息（如城市、景点地址等）
     */
    private String extra;
}
