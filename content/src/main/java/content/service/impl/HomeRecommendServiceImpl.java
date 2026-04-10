package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import common.client.UsersClient;
import common.utils.Result;
import content.pojo.dto.destination.QueryHotDestinationDTO;
import content.pojo.entity.Attraction;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.HomeRecommendVO;
import content.pojo.vo.HotDestinationVO;
import content.service.AttractionService;
import content.service.DestinationService;
import content.service.HomeRecommendService;
import content.service.TravelNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页推荐服务实现
 */
@Slf4j
@Service
public class HomeRecommendServiceImpl implements HomeRecommendService {

    @Autowired
    private TravelNoteService travelNoteService;

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private UsersClient usersClient;

    private static final int RECOMMEND_SIZE = 6; // 推荐数量

    @Override
    public Result<HomeRecommendVO> getHomeRecommend() {
        HomeRecommendVO vo = new HomeRecommendVO();

        // 获取推荐游记（置顶优先，然后按浏览量排序）
        List<HomeRecommendVO.TravelNoteItem> travelNotes = getRecommendedTravelNotes();
        vo.setRecommendedTravelNotes(travelNotes);

        // 获取热门目的地（按浏览量排序）
        List<HomeRecommendVO.DestinationItem> destinations = getHotDestinations();
        vo.setHotDestinations(destinations);

        // 获取热门景点（按浏览量排序）
        List<HomeRecommendVO.AttractionItem> attractions = getHotAttractions();
        vo.setHotAttractions(attractions);

        return Result.success(vo);
    }

    /**
     * 获取推荐游记
     */
    private List<HomeRecommendVO.TravelNoteItem> getRecommendedTravelNotes() {
        // 先查询置顶的游记
        QueryWrapper<TravelNote> topWrapper = new QueryWrapper<>();
        topWrapper.eq("status", 1); // 已审核通过
        topWrapper.eq("sort_order", 1); // 置顶
        topWrapper.orderByDesc("created_at");
        topWrapper.last("LIMIT " + RECOMMEND_SIZE);
        List<TravelNote> topNotes = travelNoteService.list(topWrapper);

        // 如果置顶游记不足，补充热门游记
        List<TravelNote> allNotes = new ArrayList<>(topNotes);
        if (topNotes.size() < RECOMMEND_SIZE) {
            QueryWrapper<TravelNote> hotWrapper = new QueryWrapper<>();
            hotWrapper.eq("status", 1); // 已审核通过
            hotWrapper.ne("sort_order", 1); // 排除已置顶的
            hotWrapper.orderByDesc("view_count", "created_at"); // 按浏览量和创建时间排序
            hotWrapper.last("LIMIT " + (RECOMMEND_SIZE - topNotes.size()));
            List<TravelNote> hotNotes = travelNoteService.list(hotWrapper);
            allNotes.addAll(hotNotes);
        }

        // 如果还是没有数据，查询所有已发布的游记（不限制状态）
        if (allNotes.isEmpty()) {
            QueryWrapper<TravelNote> allWrapper = new QueryWrapper<>();
            allWrapper.ne("status", -1); // 排除草稿
            allWrapper.orderByDesc("view_count", "created_at");
            allWrapper.last("LIMIT " + RECOMMEND_SIZE);
            allNotes = travelNoteService.list(allWrapper);
        }

        // 收集用户ID
        List<String> userIds = allNotes.stream()
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
        return allNotes.stream().map(note -> {
            HomeRecommendVO.TravelNoteItem item = new HomeRecommendVO.TravelNoteItem();
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
            item.setTop(note.getSortOrder() != null && note.getSortOrder() == 1);

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
    }

    /**
     * 获取热门目的地
     */
    private List<HomeRecommendVO.DestinationItem> getHotDestinations() {
        QueryWrapper<Destination> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 启用状态
        wrapper.orderByDesc("view_count", "created_at"); // 按浏览量和创建时间排序
        wrapper.last("LIMIT " + RECOMMEND_SIZE);
        List<Destination> destinations = destinationService.list(wrapper);

        return destinations.stream().map(dest -> {
            HomeRecommendVO.DestinationItem item = new HomeRecommendVO.DestinationItem();
            item.setDestinationId(dest.getDestinationId());
            item.setName(dest.getName());
            item.setCoverImg(dest.getCoverImg());
            item.setDescription(dest.getDescription());
            item.setViewCount(dest.getViewCount());
            item.setProvince(dest.getProvince());
            item.setCity(dest.getCity());
            item.setBestSeason(dest.getBestSeason());
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 获取热门景点
     */
    private List<HomeRecommendVO.AttractionItem> getHotAttractions() {
        QueryWrapper<Attraction> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 启用状态
        wrapper.orderByDesc("view_count", "created_at"); // 按浏览量和创建时间排序
        wrapper.last("LIMIT " + RECOMMEND_SIZE);
        List<Attraction> attractions = attractionService.list(wrapper);

        // 如果没有数据，查询所有景点（不限制状态）
        if (attractions.isEmpty()) {
            QueryWrapper<Attraction> allWrapper = new QueryWrapper<>();
            allWrapper.orderByDesc("view_count", "created_at");
            allWrapper.last("LIMIT " + RECOMMEND_SIZE);
            attractions = attractionService.list(allWrapper);
        }

        // 收集目的地ID
        List<String> destinationIds = attractions.stream()
                .map(Attraction::getDestinationId)
                .distinct()
                .collect(Collectors.toList());

        // 批量获取目的地信息
        final Map<String, Destination> destinationMap;
        if (!destinationIds.isEmpty()) {
            QueryWrapper<Destination> destWrapper = new QueryWrapper<>();
            destWrapper.in("destination_id", destinationIds);
            List<Destination> destinations = destinationService.list(destWrapper);
            destinationMap = destinations.stream()
                    .collect(Collectors.toMap(Destination::getDestinationId, d -> d));
        } else {
            destinationMap = new HashMap<>();
        }

        return attractions.stream().map(attr -> {
            HomeRecommendVO.AttractionItem item = new HomeRecommendVO.AttractionItem();
            item.setAid(attr.getAid());
            item.setDestinationId(attr.getDestinationId());
            item.setName(attr.getName());
            item.setCoverImg(attr.getImages() != null && !attr.getImages().isEmpty()
                    ? attr.getImages().get(0) : "");
            item.setDescription(attr.getDescription());
            item.setViewCount(attr.getViewCount());

            // 设置目的地名称
            Destination dest = destinationMap.get(attr.getDestinationId());
            if (dest != null) {
                item.setDestinationName(dest.getName());
            } else {
                item.setDestinationName("");
            }

            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 获取热门目的地排行
     * 游客和普通用户都可以访问
     * @param dto 查询参数
     * @return 热门目的地排行
     */
    public Result<HotDestinationVO> getHotDestinationRanking(QueryHotDestinationDTO dto) {
        QueryWrapper<Destination> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 启用状态
        wrapper.orderByDesc("view_count", "created_at"); // 按浏览量和创建时间排序
        wrapper.last("LIMIT " + dto.getLimit());
        List<Destination> destinations = destinationService.list(wrapper);

        // 转换为VO并添加排名
        List<HotDestinationVO.DestinationRankingItem> rankingItems = new ArrayList<>();
        for (int i = 0; i < destinations.size(); i++) {
            Destination dest = destinations.get(i);
            HotDestinationVO.DestinationRankingItem item = new HotDestinationVO.DestinationRankingItem();
            item.setRanking(i + 1); // 排名从1开始
            item.setDestinationId(dest.getDestinationId());
            item.setName(dest.getName());
            item.setCoverImg(dest.getCoverImg());
            item.setDescription(dest.getDescription());
            item.setViewCount(dest.getViewCount());
            item.setProvince(dest.getProvince());
            item.setCity(dest.getCity());
            item.setBestSeason(dest.getBestSeason());
            rankingItems.add(item);
        }

        HotDestinationVO vo = new HotDestinationVO();
        vo.setDestinations(rankingItems);

        return Result.success(vo);
    }
}
