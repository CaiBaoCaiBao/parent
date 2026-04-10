package content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import content.pojo.entity.SearchLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 搜索记录Mapper
 */
@Mapper
public interface SearchLogMapper extends BaseMapper<SearchLog> {

    /**
     * 获取热门搜索关键词（最近N天）
     *
     * @param days 天数
     * @param limit 限制数量
     * @return 热门关键词列表
     */
    @Select("SELECT keyword, COUNT(*) as search_count, " +
            "COUNT(DISTINCT user_id) as unique_user_count, " +
            "MAX(created_at) as last_search_time " +
            "FROM search_log " +
            "WHERE created_at >= NOW() - INTERVAL '${days} days' " +
            "GROUP BY keyword " +
            "ORDER BY search_count DESC, unique_user_count DESC, last_search_time DESC " +
            "LIMIT #{limit}")
    List<content.pojo.vo.search.HotKeywordVO> getHotKeywords(int days, int limit);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 搜索历史列表
     */
    @Select("SELECT DISTINCT keyword, MAX(created_at) as last_search_time " +
            "FROM search_log " +
            "WHERE user_id = #{userId} " +
            "GROUP BY keyword " +
            "ORDER BY last_search_time DESC " +
            "LIMIT #{limit}")
    List<content.pojo.vo.search.HotKeywordVO> getUserSearchHistory(String userId, int limit);
}
