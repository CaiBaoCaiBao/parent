package content.service.impl;

import common.context.UserContext;
import common.utils.Result;
import content.mapper.SearchLogMapper;
import content.pojo.dto.search.HotKeywordsDTO;
import content.pojo.dto.search.SaveSearchLogDTO;
import content.pojo.entity.SearchLog;
import content.pojo.vo.search.HotKeywordVO;
import content.service.SearchLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 搜索记录服务实现
 */
@Slf4j
@Service
public class SearchLogServiceImpl implements SearchLogService {

    private static final String HOT_KEYWORDS_CACHE_KEY = "search:hot_keywords";
    private static final long HOT_KEYWORDS_CACHE_TTL = 30; // 缓存30分钟

    @Autowired
    private SearchLogMapper searchLogMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> saveSearchLog(SaveSearchLogDTO dto) {
        try {
            SearchLog searchLog = new SearchLog();
            searchLog.setKeyword(dto.getKeyword());
            searchLog.setSearchType(dto.getSearchType());
            searchLog.setResultCount(dto.getResultCount());
            searchLog.setIpAddress(dto.getIpAddress());
            searchLog.setUserAgent(dto.getUserAgent());

            // 获取当前用户ID（如果已登录）
            String userId = UserContext.getUserUUid();
            if (userId != null && !userId.isEmpty()) {
                searchLog.setUserId(userId);
            }

            searchLogMapper.insert(searchLog);
            log.info("保存搜索记录成功，关键词：{}，用户ID：{}", dto.getKeyword(), userId);

            // 清除热门搜索缓存
            clearHotKeywordsCache();

            return Result.success();
        } catch (Exception e) {
            log.error("保存搜索记录失败，关键词：{}", dto.getKeyword(), e);
            // 搜索记录保存失败不影响主流程，返回成功
            return Result.success();
        }
    }

    @Override
    public Result<?> getHotKeywords(HotKeywordsDTO dto) {
        try {
            // 先尝试从缓存获取
            List<String> cachedKeywords = getHotKeywordsFromCache();
            if (cachedKeywords != null && !cachedKeywords.isEmpty()) {
                log.info("从缓存获取热门搜索关键词成功，数量：{}", cachedKeywords.size());
                return Result.success(cachedKeywords);
            }

            // 缓存未命中，从数据库查询
            List<HotKeywordVO> hotKeywords = searchLogMapper.getHotKeywords(30, dto.getLimit());

            // 如果没有数据，返回默认热门搜索词
            if (hotKeywords.isEmpty()) {
                log.info("暂无搜索记录，返回默认热门搜索词");
                List<String> defaultKeywords = getDefaultHotKeywords();
                // 缓存默认关键词
                setHotKeywordsToCache(defaultKeywords);
                return Result.success(defaultKeywords);
            }

            // 提取关键词列表
            List<String> keywords = hotKeywords.stream()
                    .map(HotKeywordVO::getKeyword)
                    .toList();

            // 缓存热门搜索关键词
            setHotKeywordsToCache(keywords);

            log.info("获取热门搜索关键词成功，数量：{}", keywords.size());
            return Result.success(keywords);
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败", e);
            // 出错时返回默认热门搜索词
            return Result.success(getDefaultHotKeywords());
        }
    }

    @Override
    public Result<?> getUserSearchHistory(String userId, int limit) {
        try {
            List<HotKeywordVO> history = searchLogMapper.getUserSearchHistory(userId, limit);
            log.info("获取用户搜索历史成功，用户ID：{}，数量：{}", userId, history.size());
            return Result.success(history);
        } catch (Exception e) {
            log.error("获取用户搜索历史失败，用户ID：{}", userId, e);
            return Result.error("获取搜索历史失败");
        }
    }

    /**
     * 获取默认热门搜索词
     */
    private List<String> getDefaultHotKeywords() {
        return List.of(
                "北京", "上海", "西湖", "故宫", "长城", "三亚", "成都", "西安",
                "九寨沟", "张家界", "黄山", "泰山", "丽江", "桂林", "厦门", "青岛"
        );
    }

    /**
     * 从缓存获取热门搜索关键词
     */
    @SuppressWarnings("unchecked")
    private List<String> getHotKeywordsFromCache() {
        try {
            Object cached = redisTemplate.opsForValue().get(HOT_KEYWORDS_CACHE_KEY);
            if (cached instanceof List) {
                return (List<String>) cached;
            }
            return null;
        } catch (Exception e) {
            log.error("从缓存获取热门搜索关键词失败", e);
            return null;
        }
    }

    /**
     * 将热门搜索关键词设置到缓存
     */
    private void setHotKeywordsToCache(List<String> keywords) {
        try {
            redisTemplate.opsForValue().set(
                    HOT_KEYWORDS_CACHE_KEY,
                    keywords,
                    HOT_KEYWORDS_CACHE_TTL,
                    TimeUnit.MINUTES
            );
            log.info("热门搜索关键词已缓存，TTL：{}分钟", HOT_KEYWORDS_CACHE_TTL);
        } catch (Exception e) {
            log.error("缓存热门搜索关键词失败", e);
        }
    }

    /**
     * 清除热门搜索缓存
     */
    private void clearHotKeywordsCache() {
        try {
            redisTemplate.delete(HOT_KEYWORDS_CACHE_KEY);
            log.info("热门搜索缓存已清除");
        } catch (Exception e) {
            log.error("清除热门搜索缓存失败", e);
        }
    }
}
