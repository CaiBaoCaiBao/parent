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
        if (!Objects.equals(dto.getTargetType(), "travel_note")) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "收藏目标类型无效");
        }

        // 查询是否已收藏
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUid)
                .eq("target_type", dto.getTargetType())
                .eq("target_id", dto.getTargetId());
        Collection existingCollection = collectionMapper.selectOne(queryWrapper);

        if (existingCollection != null) {
            // 已收藏，执行取消收藏
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

        // 收集所有目标ID
        List<String> targetIds = collectionPage.getRecords().stream()
                .map(Collection::getTargetId)
                .collect(Collectors.toList());

        // 批量获取游记详情
        Map<String, Map<String, Object>> travelNoteMap = new HashMap<>();
        if (!targetIds.isEmpty()) {
            Result<?> batchResult = contentClient.getBatchTravelNoteDetail(targetIds);
            if (batchResult != null && batchResult.getSuccess() && batchResult.getData() != null) {
                List<Map<String, Object>> travelNotes = (List<Map<String, Object>>) batchResult.getData();
                for (Map<String, Object> note : travelNotes) {
                    String noteId = (String) note.get("noteId");
                    travelNoteMap.put(noteId, note);
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

            // 从内容服务获取目标信息（标题、封面）
            Map<String, Object> travelNote = travelNoteMap.get(collection.getTargetId());
            if (travelNote != null) {
                vo.setTargetTitle((String) travelNote.get("title"));
                vo.setTargetCover((String) travelNote.get("coverImg"));
            } else {
                vo.setTargetTitle("游记标题");
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
}
