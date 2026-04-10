package social.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.client.ContentClient;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import social.mapper.CollectionMapper;
import social.pojo.dto.collection.CheckCollectionStatusDTO;
import social.pojo.dto.collection.QueryCollectionListDTO;
import social.pojo.dto.collection.ToggleCollectionDTO;
import social.pojo.entity.Collection;
import social.pojo.vo.CollectionStatusVO;
import social.pojo.vo.CollectionVO;
import social.service.CollectionService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, Collection> implements CollectionService {

    @Autowired
    private CollectionMapper collectionMapper;

    @Autowired
    private ContentClient contentClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> toggleCollection(ToggleCollectionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();

        // 验证用户状态
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("收藏操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 验证目标类型
        if (!Objects.equals(dto.getTargetType(), "travel_note") &&
            !Objects.equals(dto.getTargetType(), "destination") &&
            !Objects.equals(dto.getTargetType(), "attraction")) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "收藏目标类型无效");
        }

        // 如果是游记，检查游记状态
        if ("travel_note".equals(dto.getTargetType())) {
            try {
                Result<?> travelNoteResult = contentClient.getTravelNoteDetail(dto.getTargetId());
                if (travelNoteResult == null || !travelNoteResult.getSuccess() || travelNoteResult.getData() == null) {
                    return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
                }
                Map<String, Object> travelNoteData = (Map<String, Object>) travelNoteResult.getData();
                Integer travelNoteStatus = (Integer) travelNoteData.get("status");
                // 只有已发布的游记才能收藏
                if (!DictConstants.TravelNoteStatus.PUBLISHED.equals(travelNoteStatus)) {
                    return Result.error(ResultCode.FORBIDDEN.getCode(), "只能收藏已发布的游记");
                }
            } catch (Exception e) {
                log.error("检查游记状态失败: targetId={}", dto.getTargetId(), e);
                return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "检查游记状态失败");
            }
        }

        // 查询是否已收藏
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid)
                .eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Collection existingCollection = collectionMapper.selectOne(queryWrapper);

        if (existingCollection != null) {
            // 已收藏，执行取消收藏（物理删除）
            collectionMapper.deleteById(existingCollection.getId());
            return Result.success("取消收藏成功");
        } else {
            // 未收藏，执行收藏
            Collection collection = new Collection();
            // 生成收藏ID（使用 "COLLECTION_" + ULID 格式，使ID更有语义）
            collection.setCollectionId("COLLECTION_" + common.utils.ULIDUtils.generateULID());
            collection.setUserId(currentUid);
            collection.setTargetType(dto.getTargetType());
            collection.setTargetId(dto.getTargetId());
            collection.setCreatedAt(LocalDateTime.now());
            collection.setUpdatedAt(LocalDateTime.now());
            collectionMapper.insert(collection);
            return Result.success("收藏成功");
        }
    }

    @Override
    public Result<CollectionStatusVO> checkCollectionStatus(CheckCollectionStatusDTO dto) {
        String currentUid = UserContext.getUserUUid();
        log.info("检查收藏状态 - 用户ID: {}, 目标类型: {}, 目标ID: {}", currentUid, dto.getTargetType(), dto.getTargetId());

        // 查询收藏状态
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid)
                .eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Collection existingCollection = collectionMapper.selectOne(queryWrapper);

        // 查询收藏总数
        QueryWrapper<Collection> countWrapper = new QueryWrapper<>();
        countWrapper.eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Long collectionCount = collectionMapper.selectCount(countWrapper);

        CollectionStatusVO vo = new CollectionStatusVO();
        vo.setIsCollected(existingCollection != null);
        vo.setCollectionCount(collectionCount.intValue());

        log.info("收藏状态结果 - 是否收藏: {}, 收藏数: {}", vo.getIsCollected(), vo.getCollectionCount());
        return Result.success(vo);
    }

    @Override
    public Result<?> queryCollectionList(QueryCollectionListDTO dto) {
        String currentUid = UserContext.getUserUUid();

        // 构建查询条件
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid);
        if (StringUtils.hasText(dto.getTargetType())) {
            queryWrapper.eq("target_type", dto.getTargetType());
        }
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<Collection> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Collection> collectionPage = collectionMapper.selectPage(page, queryWrapper);

        // 收集所有目标ID，按类型分组
        Map<String, List<String>> targetIdsByType = new HashMap<>();
        for (Collection collection : collectionPage.getRecords()) {
            String type = collection.getTargetType();
            String id = collection.getTargetId();
            targetIdsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(id);
        }

        // 批量获取不同类型的内容详情
        Map<String, Map<String, Object>> travelNoteMap = new HashMap<>();
        Map<String, Map<String, Object>> destinationMap = new HashMap<>();
        Map<String, Map<String, Object>> attractionMap = new HashMap<>();

        // 获取游记详情
        if (targetIdsByType.containsKey("travel_note")) {
            List<String> travelIds = targetIdsByType.get("travel_note");
            if (!travelIds.isEmpty()) {
                Result<?> batchResult = contentClient.getBatchTravelNoteDetail(travelIds);
                if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                    List<Map<String, Object>> travelNotes = (List<Map<String, Object>>) batchResult.getData();
                    for (Map<String, Object> note : travelNotes) {
                        String noteId = (String) note.get("noteId");
                        travelNoteMap.put(noteId, note);
                    }
                }
            }
        }

        // 获取目的地详情
        if (targetIdsByType.containsKey("destination")) {
            List<String> destinationIds = targetIdsByType.get("destination");
            if (!destinationIds.isEmpty()) {
                Result<?> batchResult = contentClient.getBatchDestinationDetail(destinationIds);
                if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                    List<Map<String, Object>> destinations = (List<Map<String, Object>>) batchResult.getData();
                    for (Map<String, Object> dest : destinations) {
                        String destId = (String) dest.get("destinationId");
                        destinationMap.put(destId, dest);
                    }
                }
            }
        }

        // 获取景点详情
        if (targetIdsByType.containsKey("attraction")) {
            List<String> attractionIds = targetIdsByType.get("attraction");
            if (!attractionIds.isEmpty()) {
                Result<?> batchResult = contentClient.getBatchAttractionDetail(attractionIds);
                if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                    List<Map<String, Object>> attractions = (List<Map<String, Object>>) batchResult.getData();
                    for (Map<String, Object> attraction : attractions) {
                        String attractionId = (String) attraction.get("aid");
                        attractionMap.put(attractionId, attraction);
                    }
                }
            }
        }

        // 转换为VO
        List<CollectionVO> voList = collectionPage.getRecords().stream().map(collection -> {
            CollectionVO vo = new CollectionVO();
            vo.setCollectionId(collection.getCollectionId());
            vo.setUserId(collection.getUserId());
            vo.setTargetType(collection.getTargetType());
            vo.setTargetId(collection.getTargetId());
            vo.setCreatedAt(collection.getCreatedAt());

            // 根据不同类型从内容服务获取目标信息（标题、封面）
            String targetType = collection.getTargetType();
            String targetId = collection.getTargetId();

            if ("travel_note".equals(targetType)) {
                Map<String, Object> travelNote = travelNoteMap.get(targetId);
                if (travelNote != null) {
                    vo.setTargetTitle((String) travelNote.get("title"));
                    vo.setTargetCover((String) travelNote.get("coverImg"));
                } else {
                    vo.setTargetTitle("游记标题");
                    vo.setTargetCover("");
                }
            } else if ("destination".equals(targetType)) {
                Map<String, Object> destination = destinationMap.get(targetId);
                if (destination != null) {
                    vo.setTargetTitle((String) destination.get("name"));
                    vo.setTargetCover((String) destination.get("coverImg"));
                } else {
                    vo.setTargetTitle("目的地名称");
                    vo.setTargetCover("");
                }
            } else if ("attraction".equals(targetType)) {
                Map<String, Object> attraction = attractionMap.get(targetId);
                if (attraction != null) {
                    vo.setTargetTitle((String) attraction.get("name"));
                    Object images = attraction.get("images");
                    if (images != null) {
                        if (images instanceof List) {
                            List<?> imageList = (List<?>) images;
                            if (!imageList.isEmpty()) {
                                vo.setTargetCover(imageList.get(0).toString());
                            } else {
                                vo.setTargetCover("");
                            }
                        } else {
                            vo.setTargetCover(images.toString());
                        }
                    } else {
                        vo.setTargetCover("");
                    }
                } else {
                    vo.setTargetTitle("景点名称");
                    vo.setTargetCover("");
                }
            } else {
                vo.setTargetTitle("未知类型");
                vo.setTargetCover("");
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", collectionPage.getTotal());
        result.put("page", dto.getPage());
        result.put("pageSize", dto.getPageSize());

        return Result.success(result);
    }

    @Override
    public Result<java.util.Map<String, Integer>> getBatchCollectionCount(java.util.List<String> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Result.success(new java.util.HashMap<>());
        }

        // 批量查询收藏数量（支持所有类型）
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("target_id", targetIds);
        java.util.List<Collection> collectionList = collectionMapper.selectList(queryWrapper);

        // 统计每个目标的收藏数
        java.util.Map<String, Integer> countMap = new java.util.HashMap<>();
        for (String targetId : targetIds) {
            countMap.put(targetId, 0);
        }

        for (Collection collection : collectionList) {
            String targetId = collection.getTargetId();
            countMap.put(targetId, countMap.getOrDefault(targetId, 0) + 1);
        }

        return Result.success(countMap);
    }

    @Override
    public Result<Integer> getCollectionCount(String targetId) {
        if (targetId == null || targetId.isEmpty()) {
            return Result.success(0);
        }

        // 查询收藏数量（支持所有类型）
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("target_id", targetId);
        Long collectionCount = collectionMapper.selectCount(queryWrapper);

        return Result.success(collectionCount.intValue());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deleteCollectionsByTargetIds(String targetType, List<String> targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标ID列表不能为空");
        }

        if (!StringUtils.hasText(targetType)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目标类型不能为空");
        }

        try {
            // 删除指定目标类型和目标ID列表的所有收藏
            QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("target_type", targetType);
            queryWrapper.in("target_id", targetIds);

            int deletedCount = collectionMapper.delete(queryWrapper);
            log.info("批量删除收藏成功: targetType={}, targetIds={}, deletedCount={}", targetType, targetIds, deletedCount);

            return Result.success("删除成功，共删除" + deletedCount + "条收藏记录");
        } catch (Exception e) {
            log.error("批量删除收藏失败: targetType={}, targetIds={}", targetType, targetIds, e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除收藏失败");
        }
    }
}
