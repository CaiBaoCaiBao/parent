package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.client.UsersClient;
import common.utils.Result;
import content.pojo.dto.search.HotKeywordsDTO;
import content.pojo.dto.search.SearchDTO;
import content.pojo.dto.search.SearchSuggestionDTO;
import content.pojo.entity.Attraction;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.search.HotKeywordVO;
import content.pojo.vo.search.SearchResultVO;
import content.pojo.vo.search.SearchSuggestionVO;
import content.service.AttractionService;
import content.service.DestinationService;
import content.service.SearchLogService;
import content.service.SearchService;
import content.service.TravelNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private TravelNoteService travelNoteService;

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private UsersClient usersClient;

    @Autowired
    private SearchLogService searchLogService;

    @Override
    public Result<?> search(SearchDTO dto) {
        log.info("开始搜索，关键词：{}，类型：{}，页码：{}，每页大小：{}", dto.getKeyword(), dto.getType(), dto.getPageNum(), dto.getPageSize());
        SearchResultVO result = new SearchResultVO();

        // 根据搜索类型决定搜索内容
        if ("all".equals(dto.getType()) || "travel_note".equals(dto.getType())) {
            // 搜索游记
            Result<?> travelNoteResult = searchTravelNotes(dto);
            if (travelNoteResult.getSuccess()) {
                Map<String, Object> travelNoteData = (Map<String, Object>) travelNoteResult.getData();
                result.setTravelNotes((List<SearchResultVO.TravelNoteItem>) travelNoteData.get("list"));
                result.setTravelNoteTotal((Long) travelNoteData.get("total"));
                log.info("游记搜索完成，总数：{}", result.getTravelNoteTotal());
            }
        }

        if ("all".equals(dto.getType()) || "destination".equals(dto.getType())) {
            // 搜索目的地
            Result<?> destinationResult = searchDestinations(dto);
            if (destinationResult.getSuccess()) {
                Map<String, Object> destinationData = (Map<String, Object>) destinationResult.getData();
                result.setDestinations((List<SearchResultVO.DestinationItem>) destinationData.get("list"));
                result.setDestinationTotal((Long) destinationData.get("total"));
                log.info("目的地搜索完成，总数：{}", result.getDestinationTotal());
            }
        }

        if ("all".equals(dto.getType()) || "attraction".equals(dto.getType())) {
            // 搜索景点
            Result<?> attractionResult = searchAttractions(dto);
            if (attractionResult.getSuccess()) {
                Map<String, Object> attractionData = (Map<String, Object>) attractionResult.getData();
                result.setAttractions((List<SearchResultVO.AttractionItem>) attractionData.get("list"));
                result.setAttractionTotal((Long) attractionData.get("total"));
                log.info("景点搜索完成，总数：{}", result.getAttractionTotal());
            }
        }

        log.info("搜索完成，返回结果：{}", result);
        return Result.success(result);
    }

    @Override
    public Result<?> getSuggestions(SearchSuggestionDTO dto) {
        log.info("获取搜索建议，关键词：{}，限制：{}", dto.getKeyword(), dto.getLimit());

        List<SearchSuggestionVO> suggestions = new ArrayList<>();
        String keyword = dto.getKeyword().trim();

        if (!StringUtils.hasText(keyword)) {
            return Result.success(suggestions);
        }

        // 搜索目的地建议
        QueryWrapper<Destination> destQuery = new QueryWrapper<>();
        destQuery.like("name", keyword)
                .or()
                .like("city", keyword)
                .or()
                .like("province", keyword)
                .last("LIMIT " + dto.getLimit());
        destQuery.orderByDesc("created_at");

        List<Destination> destinations = destinationService.list(destQuery);
        for (Destination dest : destinations) {
            SearchSuggestionVO suggestion = new SearchSuggestionVO();
            suggestion.setKeyword(dest.getName());
            suggestion.setType("destination");
            suggestion.setId(dest.getDestinationId());
            suggestion.setExtra(dest.getProvince() != null ? dest.getProvince() + " · " + dest.getCity() : dest.getCity());
            suggestions.add(suggestion);
        }

        // 搜索景点建议
        QueryWrapper<Attraction> attrQuery = new QueryWrapper<>();
        attrQuery.eq("status", 1)
                .and(wrapper -> wrapper
                        .like("name", keyword)
                        .or()
                        .like("address", keyword)
                )
                .last("LIMIT " + dto.getLimit());
        attrQuery.orderByDesc("created_at");

        List<Attraction> attractions = attractionService.list(attrQuery);
        for (Attraction attr : attractions) {
            SearchSuggestionVO suggestion = new SearchSuggestionVO();
            suggestion.setKeyword(attr.getName());
            suggestion.setType("attraction");
            suggestion.setId(attr.getAid());
            suggestion.setExtra(attr.getAddress());
            suggestions.add(suggestion);
        }

        // 搜索游记建议
        QueryWrapper<TravelNote> noteQuery = new QueryWrapper<>();
        noteQuery.in("status", 0, 1, 2)
                .like("title", keyword)
                .last("LIMIT " + dto.getLimit());
        noteQuery.orderByDesc("created_at");

        List<TravelNote> travelNotes = travelNoteService.list(noteQuery);
        for (TravelNote note : travelNotes) {
            SearchSuggestionVO suggestion = new SearchSuggestionVO();
            suggestion.setKeyword(note.getTitle());
            suggestion.setType("travel_note");
            suggestion.setId(note.getNoteId());
            suggestion.setExtra(note.getContent() != null && note.getContent().length() > 50
                    ? note.getContent().substring(0, 50) + "..."
                    : note.getContent());
            suggestions.add(suggestion);
        }

        // 去重并限制数量
        suggestions = suggestions.stream()
                .distinct()
                .limit(dto.getLimit())
                .collect(Collectors.toList());

        log.info("搜索建议获取完成，数量：{}", suggestions.size());
        return Result.success(suggestions);
    }

    /**
     * 搜索游记
     */
    private Result<?> searchTravelNotes(SearchDTO dto) {
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();

        // 搜索状态为正常（1）或待审核（2）的游记，不搜索已删除（0）的游记
        // 注意：根据数据库设计，status=0表示删除，status=1表示正常，status=2表示待审核
        // 但实际数据中status=0的记录可能需要被搜索到，所以这里改为搜索所有非逻辑删除的记录
        queryWrapper.in("status", 0, 1, 2);

        // 标题模糊搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", dto.getKeyword())
                    .or()
                    .like("content", dto.getKeyword())
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("created_at");

        log.info("游记查询条件：{}", queryWrapper);

        // 分页查询
        Page<TravelNote> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        log.info("游记分页参数：页码：{}，每页大小：{}", page.getCurrent(), page.getSize());
        Page<TravelNote> travelNotePage = travelNoteService.page(page, queryWrapper);

        log.info("游记分页查询结果：总记录数：{}，当前页记录数：{}，查询SQL：{}", travelNotePage.getTotal(), travelNotePage.getRecords().size(), queryWrapper.getCustomSqlSegment());

        // 收集用户ID
        List<String> userIds = travelNotePage.getRecords().stream()
                .map(TravelNote::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取用户信息
        Map<String, Map<String, Object>> userInfoMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            Result<?> batchResult = usersClient.getBatchUserInfo(userIds);
            if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                List<Map<String, Object>> userInfoList = (List<Map<String, Object>>) batchResult.getData();
                for (Map<String, Object> userInfo : userInfoList) {
                    // 兼容两种字段名：uUid 和 uuid
                    String uUid = (String) userInfo.get("uUid");
                    if (uUid == null) {
                        uUid = (String) userInfo.get("uuid");
                    }
                    userInfoMap.put(uUid, userInfo);
                }
            }
        }

        // 转换为VO
        List<SearchResultVO.TravelNoteItem> voList = travelNotePage.getRecords().stream().map(note -> {
            SearchResultVO.TravelNoteItem item = new SearchResultVO.TravelNoteItem();
            item.setNoteId(note.getNoteId());
            item.setTitle(note.getTitle());
            item.setCoverImg(note.getCoverImg());
            // 从内容中截取前100个字符作为摘要
            String summary = note.getContent() != null && note.getContent().length() > 100
                    ? note.getContent().substring(0, 100) + "..."
                    : note.getContent();
            item.setSummary(summary);
            item.setUserId(note.getUserId());
            item.setViewCount(note.getViewCount());
            item.setLikeCount(note.getLikeCount());
            item.setCommentCount(note.getCommentCount());
            item.setCreatedAt(note.getCreatedAt().toString());

            // 设置用户信息
            Map<String, Object> userInfo = userInfoMap.get(note.getUserId());
            if (userInfo != null) {
                item.setUserName((String) userInfo.get("nickName"));
                item.setUserAvatar((String) userInfo.get("avatar"));
            } else {
                item.setUserName("未知用户");
                item.setUserAvatar("");
            }

            return item;
        }).collect(Collectors.toList());

        log.info("游记VO转换完成，列表大小：{}", voList.size());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", travelNotePage.getTotal());
        result.put("page", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }

    /**
     * 搜索目的地
     */
    private Result<?> searchDestinations(SearchDTO dto) {
        QueryWrapper<Destination> queryWrapper = new QueryWrapper<>();

        // 名称模糊搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", dto.getKeyword())
                    .or()
                    .like("description", dto.getKeyword())
                    .or()
                    .like("province", dto.getKeyword())
                    .or()
                    .like("city", dto.getKeyword())
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<Destination> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        log.info("目的地分页参数：页码：{}，每页大小：{}", page.getCurrent(), page.getSize());
        Page<Destination> destinationPage = destinationService.page(page, queryWrapper);

        log.info("目的地分页查询结果：总记录数：{}，当前页记录数：{}，查询SQL：{}", destinationPage.getTotal(), destinationPage.getRecords().size(), queryWrapper.getCustomSqlSegment());

        // 转换为VO
        List<SearchResultVO.DestinationItem> voList = destinationPage.getRecords().stream().map(dest -> {
            SearchResultVO.DestinationItem item = new SearchResultVO.DestinationItem();
            item.setDestinationId(dest.getDestinationId());
            item.setName(dest.getName());
            item.setCoverImg(dest.getCoverImg());
            item.setDescription(dest.getDescription());
            item.setProvince(dest.getProvince());
            item.setCity(dest.getCity());
            item.setCreatedAt(dest.getCreatedAt().toString());
            return item;
        }).collect(Collectors.toList());

        log.info("目的地VO转换完成，列表大小：{}", voList.size());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", destinationPage.getTotal());
        result.put("page", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }

    /**
     * 搜索景点
     */
    private Result<?> searchAttractions(SearchDTO dto) {
        QueryWrapper<Attraction> queryWrapper = new QueryWrapper<>();

        // 搜索状态为上架（1）的景点
        queryWrapper.eq("status", 1);

        // 名称、描述、地址模糊搜索
        if (StringUtils.hasText(dto.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", dto.getKeyword())
                    .or()
                    .like("description", dto.getKeyword())
                    .or()
                    .like("address", dto.getKeyword())
            );
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<Attraction> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        log.info("景点分页参数：页码：{}，每页大小：{}", page.getCurrent(), page.getSize());
        Page<Attraction> attractionPage = attractionService.page(page, queryWrapper);

        log.info("景点分页查询结果：总记录数：{}，当前页记录数：{}，查询SQL：{}", attractionPage.getTotal(), attractionPage.getRecords().size(), queryWrapper.getCustomSqlSegment());

        // 转换为VO
        List<SearchResultVO.AttractionItem> voList = attractionPage.getRecords().stream().map(attraction -> {
            SearchResultVO.AttractionItem item = new SearchResultVO.AttractionItem();
            item.setAid(attraction.getAid());
            item.setDestinationId(attraction.getDestinationId());
            item.setName(attraction.getName());
            item.setImages(attraction.getImages());
            item.setAddress(attraction.getAddress());
            item.setPhone(attraction.getPhone());
            item.setDescription(attraction.getDescription());
//            item.setLongitude(attraction.getLongitude());
//            item.setLatitude(attraction.getLatitude());
            item.setViewCount(attraction.getViewCount());
            item.setCreatedAt(attraction.getCreatedAt().toString());
            return item;
        }).collect(Collectors.toList());

        log.info("景点VO转换完成，列表大小：{}", voList.size());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", attractionPage.getTotal());
        result.put("page", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<?> getHotKeywords(HotKeywordsDTO dto) {
        log.info("获取热门搜索关键词，限制：{}", dto.getLimit());

        try {
            // 调用搜索记录服务获取热门搜索
            Result<?> result = searchLogService.getHotKeywords(dto);

            if (result.getSuccess() && result.getData() != null) {
                // 如果返回的是HotKeywordVO列表，提取关键词
                if (result.getData() instanceof List) {
                    List<?> list = (List<?>) result.getData();
                    if (!list.isEmpty() && list.get(0) instanceof HotKeywordVO) {
                        List<String> keywords = ((List<HotKeywordVO>) list).stream()
                                .map(HotKeywordVO::getKeyword)
                                .collect(Collectors.toList());
                        log.info("热门搜索关键词获取完成，数量：{}", keywords.size());
                        return Result.success(keywords);
                    }
                }
                // 如果返回的是String列表，直接返回
                if (result.getData() instanceof List) {
                    List<?> list = (List<?>) result.getData();
                    if (!list.isEmpty() && list.get(0) instanceof String) {
                        return result;
                    }
                }
            }

            // 如果没有数据或出错，返回默认热门搜索词
            log.info("暂无搜索记录，返回默认热门搜索词");
            return Result.success(getDefaultHotKeywords());
        } catch (Exception e) {
            log.error("获取热门搜索关键词失败", e);
            return Result.success(getDefaultHotKeywords());
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
}
