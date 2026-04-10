package content.service;

import common.utils.Result;
import content.pojo.dto.search.HotKeywordsDTO;
import content.pojo.dto.search.SaveSearchLogDTO;
import content.pojo.vo.search.HotKeywordVO;

import java.util.List;

/**
 * 搜索记录服务
 */
public interface SearchLogService {

    /**
     * 保存搜索记录
     *
     * @param dto 搜索记录DTO
     * @return 保存结果
     */
    Result<?> saveSearchLog(SaveSearchLogDTO dto);

    /**
     * 获取热门搜索关键词
     *
     * @param dto 热门搜索DTO
     * @return 热门关键词列表
     */
    Result<?> getHotKeywords(HotKeywordsDTO dto);

    /**
     * 获取用户搜索历史
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @return 搜索历史列表
     */
    Result<?> getUserSearchHistory(String userId, int limit);
}
