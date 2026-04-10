package content.pojo.vo.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 热门搜索关键词VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotKeywordVO {

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 搜索次数
     */
    private Long searchCount;

    /**
     * 独立用户数
     */
    private Long uniqueUserCount;

    /**
     * 最后搜索时间
     */
    private String lastSearchTime;
}
