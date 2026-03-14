package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.client.UsersClient;
import common.utils.Result;
import content.pojo.dto.search.SearchDTO;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.search.SearchResultVO;
import content.service.DestinationService;
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
    private UsersClient usersClient;

    @Override
    public Result<?> search(SearchDTO dto) {
        SearchResultVO result = new SearchResultVO();

        // 根据搜索类型决定搜索内容
        if ("all".equals(dto.getType()) || "travel_note".equals(dto.getType())) {
            // 搜索游记
            Result<?> travelNoteResult = searchTravelNotes(dto);
            if (travelNoteResult.getSuccess()) {
                Map<String, Object> travelNoteData = (Map<String, Object>) travelNoteResult.getData();
                result.setTravelNotes((List<SearchResultVO.TravelNoteItem>) travelNoteData.get("list"));
                result.setTravelNoteTotal((Long) travelNoteData.get("total"));
            }
        }

        if ("all".equals(dto.getType()) || "destination".equals(dto.getType())) {
            // 搜索目的地
            Result<?> destinationResult = searchDestinations(dto);
            if (destinationResult.getSuccess()) {
                Map<String, Object> destinationData = (Map<String, Object>) destinationResult.getData();
                result.setDestinations((List<SearchResultVO.DestinationItem>) destinationData.get("list"));
                result.setDestinationTotal((Long) destinationData.get("total"));
            }
        }

        return Result.success(result);
    }

    /**
     * 搜索游记
     */
    private Result<?> searchTravelNotes(SearchDTO dto) {
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();

        // 只搜索已审核通过的游记（audit_status = 1 表示通过）
        queryWrapper.eq("status", 1);
        
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

        // 分页查询
        Page<TravelNote> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<TravelNote> travelNotePage = travelNoteService.page(page, queryWrapper);

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
                    String uUid = (String) userInfo.get("uUid");
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
        Page<Destination> destinationPage = destinationService.page(page, queryWrapper);

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

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", destinationPage.getTotal());
        result.put("page", dto.getPageNum());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }
}
