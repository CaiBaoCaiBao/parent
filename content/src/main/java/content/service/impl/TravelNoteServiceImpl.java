package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.client.ContentClient;
import common.client.SocialClient;
import common.client.UsersClient;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import content.mapper.DestinationMapper;
import content.mapper.TravelNoteMapper;
import content.pojo.dto.travelnote.AuditTravelNoteDTO;
import content.pojo.dto.travelnote.CreateTravelNoteDTO;
import content.pojo.dto.travelnote.DeleteTravelNoteDTO;
import content.pojo.dto.travelnote.GetTravelNoteDetailDTO;
import content.pojo.dto.travelnote.QueryTravelNoteDTO;
import content.pojo.dto.travelnote.SetTopTravelNoteDTO;
import content.pojo.dto.travelnote.UpdateTravelNoteDTO;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.TravelNoteDetailVO;
import content.service.TravelNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TravelNoteServiceImpl
        extends ServiceImpl<TravelNoteMapper, TravelNote>
        implements TravelNoteService {
    @Autowired
    TravelNoteMapper travelNoteMapper;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    DestinationMapper destinationMapper;
    @Autowired
    UsersClient usersClient;
    @Autowired
    SocialClient socialClient;
    @Autowired
    ContentClient contentClient;

    @Override
    public Result<?> createTravelNote(CreateTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String status = UserContext.getStatus();
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建游记操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        TravelNote travelNote = new TravelNote();
        BeanUtils.copyProperties(dto, travelNote);
        // 生成游记ID（使用 "NOTE_" + ULID 格式，使ID更有语义）
        travelNote.setNoteId("NOTE_" + common.utils.ULIDUtils.generateULID());
        // 设置默认状态为待审核
        travelNote.setStatus(DictConstants.TravelNoteStatus.PENDING);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                travelNote.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "图片列表格式错误");
            }
        }
        boolean success = save(travelNote);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> saveDraft(CreateTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String status = UserContext.getStatus();
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("保存草稿操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        TravelNote travelNote = new TravelNote();
        BeanUtils.copyProperties(dto, travelNote);
        // 生成游记ID（使用 "NOTE_" + ULID 格式，使ID更有语义）
        travelNote.setNoteId("NOTE_" + common.utils.ULIDUtils.generateULID());
        // 设置状态为草稿
        travelNote.setStatus(DictConstants.TravelNoteStatus.DRAFT);
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                travelNote.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "图片列表格式错误");
            }
        }
        boolean success = save(travelNote);
        return success ? Result.success("草稿保存成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "保存失败");
    }

    @Override
    public Result<?> publishDraft(UpdateTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String status = UserContext.getStatus();
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("发布草稿操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 查询草稿
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", dto.getNoteId());
        TravelNote travelNote = getOne(queryWrapper);

        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        // 检查权限：只能发布自己的草稿
        if (!Objects.equals(travelNote.getUserId(), currentUid)) {
            log.info("发布草稿操作者：{} 没有权限发布游记：{}", currentUid, travelNote.getNoteId());
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }

        // 检查是否为草稿状态
        if (!Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.DRAFT)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "只能发布草稿状态的游记");
        }

        // 更新内容
        BeanUtils.copyProperties(dto, travelNote);
        // 设置状态为待审核
        travelNote.setStatus(DictConstants.TravelNoteStatus.PENDING);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                travelNote.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "图片列表格式错误");
            }
        }

        boolean success = updateById(travelNote);
        return success ? Result.success("发布成功，等待审核") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "发布失败");
    }

    @Override
    public Result<?> deleteTravelNote(DeleteTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除游记操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 查询要删除的游记
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("note_id", dto.getNoteIds());
        List<TravelNote> travelNotes = list(queryWrapper);

        if (travelNotes.isEmpty()) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        // 检查权限：管理员可以删除任何游记，普通用户只能删除自己的游记
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            for (TravelNote travelNote : travelNotes) {
                if (!Objects.equals(travelNote.getUserId(), currentUid)) {
                    log.info("删除游记操作者：{} 没有权限删除游记：{}", currentUid, travelNote.getNoteId());
                    return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
                }
            }
        }

        // 级联删除相关数据
        try {
            // 1. 删除游记下的评论
            Result<?> deleteCommentsResult = socialClient.deleteCommentsByTargetIds("travel_note", dto.getNoteIds());
            if (deleteCommentsResult != null && deleteCommentsResult.getSuccess()) {
                log.info("删除游记评论成功: noteIds={}", dto.getNoteIds());
            } else {
                log.warn("删除游记评论失败: noteIds={}, result={}", dto.getNoteIds(), deleteCommentsResult);
            }

            // 2. 删除游记下的点赞
            Result<?> deleteLikesResult = socialClient.deleteLikesByTargetIds("travel_note", dto.getNoteIds());
            if (deleteLikesResult != null && deleteLikesResult.getSuccess()) {
                log.info("删除游记点赞成功: noteIds={}", dto.getNoteIds());
            } else {
                log.warn("删除游记点赞失败: noteIds={}, result={}", dto.getNoteIds(), deleteLikesResult);
            }

            // 3. 删除游记下的收藏
            Result<?> deleteCollectionsResult = socialClient.deleteCollectionsByTargetIds("travel_note", dto.getNoteIds());
            if (deleteCollectionsResult != null && deleteCollectionsResult.getSuccess()) {
                log.info("删除游记收藏成功: noteIds={}", dto.getNoteIds());
            } else {
                log.warn("删除游记收藏失败: noteIds={}, result={}", dto.getNoteIds(), deleteCollectionsResult);
            }

            // 4. 删除游记
            boolean success = remove(queryWrapper);

            if (success) {
                log.info("删除游记成功: noteIds={}, operator={}", dto.getNoteIds(), currentUid);
                return Result.success("删除成功");
            } else {
                log.error("删除游记失败: noteIds={}", dto.getNoteIds());
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
            }
        } catch (Exception e) {
            log.error("删除游记及相关数据失败: noteIds={}", dto.getNoteIds(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除失败");
        }
    }

    @Override
    public Result<?> queryTravelNoteList(QueryTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();

        log.info("查询游记列表 - 参数: noteId={}, userId={}, destinationId={}, title={}, status={}, pageNum={}, pageSize={}",
                dto.getNoteId(), dto.getUserId(), dto.getDestinationId(), dto.getTitle(), dto.getStatus(), dto.getPageNum(), dto.getPageSize());
        log.info("当前用户信息 - currentUid={}, role={}", currentUid, role);

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getNoteId())) {
            queryWrapper.eq("note_id", dto.getNoteId());
        }
        if (StringUtils.hasText(dto.getUserId())) {
            queryWrapper.eq("user_id", dto.getUserId());
        }
        if (StringUtils.hasText(dto.getDestinationId())) {
            queryWrapper.eq("destination_id", dto.getDestinationId());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            queryWrapper.like("title", dto.getTitle());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        } else {
            // 默认只显示审核通过的游记，除非明确指定查询其他状态
            // 如果查询的是自己的游记，则显示所有状态（包括草稿和已驳回）
            // 如果是管理员，则显示所有状态的游记
            if (StringUtils.hasText(dto.getUserId()) && Objects.equals(dto.getUserId(), currentUid)) {
                // 查询自己的游记，显示所有状态
                log.info("查询自己的游记，显示所有状态");
            } else if (Objects.equals(role, DictConstants.UserRole.ADMIN)) {
                // 管理员查询游记，显示所有状态
                log.info("管理员查询游记，显示所有状态");
            } else {
                // 普通用户查询其他人的游记，只显示审核通过的游记
                queryWrapper.eq("status", DictConstants.TravelNoteStatus.PUBLISHED);
                log.info("查询公共游记列表，只显示审核通过的游记 (status=1)");
            }
        }
        queryWrapper.orderByDesc("created_at");
        Page<TravelNote> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<TravelNote> resultPage = page(page, queryWrapper);

        log.info("查询结果 - 总数: {}, 当前页记录数: {}", resultPage.getTotal(), resultPage.getRecords().size());

        // 如果查询的是已发布(status=1)但没有数据，则fallback到查询所有审核通过的游记
        if (dto.getStatus() != null && dto.getStatus().equals(DictConstants.TravelNoteStatus.PUBLISHED)
                && resultPage.getRecords().isEmpty()) {
            log.info("查询已发布游记无数据，fallback到查询所有审核通过的游记");
            QueryWrapper<TravelNote> fallbackWrapper = new QueryWrapper<>();
            if (StringUtils.hasText(dto.getNoteId())) {
                fallbackWrapper.eq("note_id", dto.getNoteId());
            }
            if (StringUtils.hasText(dto.getUserId())) {
                fallbackWrapper.eq("user_id", dto.getUserId());
            }
            if (StringUtils.hasText(dto.getDestinationId())) {
                fallbackWrapper.eq("destination_id", dto.getDestinationId());
            }
            if (StringUtils.hasText(dto.getTitle())) {
                fallbackWrapper.like("title", dto.getTitle());
            }
            // 只显示审核通过的游记
            fallbackWrapper.eq("status", DictConstants.TravelNoteStatus.PUBLISHED);
            fallbackWrapper.orderByDesc("created_at");
            resultPage = page(page, fallbackWrapper);
        }

        // 批量获取用户信息
        if (!resultPage.getRecords().isEmpty()) {
            List<String> userIds = resultPage.getRecords().stream()
                    .map(TravelNote::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            log.info("游记列表查询到的用户ID列表: {}", userIds);

            try {
                Result<?> batchUserInfoResult = usersClient.getBatchUserInfo(userIds);
                log.info("批量获取用户信息结果: {}", batchUserInfoResult);

                if (batchUserInfoResult == null) {
                    log.error("批量获取用户信息失败: 返回结果为null");
                } else if (!batchUserInfoResult.getSuccess()) {
                    log.error("批量获取用户信息失败: code={}, message={}",
                            batchUserInfoResult.getCode(), batchUserInfoResult.getMessage());
                } else if (batchUserInfoResult.getData() == null) {
                    log.error("批量获取用户信息失败: data为null");
                } else {
                    // 使用 ObjectMapper 将 LinkedHashMap 转换为 UserInfoVo 列表
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchUserInfoResult.getData();
                    log.info("用户信息列表大小: {}", rawDataList.size());

                    // 创建用户信息映射
                    Map<String, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
                    for (Map<String, Object> userData : rawDataList) {
                        // 兼容两种字段名：uUid 和 uuid
                        String uUid = (String) userData.get("uUid");
                        if (uUid == null) {
                            uUid = (String) userData.get("uuid");
                        }
                        if (uUid != null) {
                            userInfoMap.put(uUid, userData);
                        }
                    }

                    // 为每个游记设置用户信息
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        Map<String, Object> userInfo = userInfoMap.get(travelNote.getUserId());
                        log.info("游记ID: {}, 用户ID: {}, 找到的用户信息: {}",
                                travelNote.getNoteId(), travelNote.getUserId(), userInfo != null ? "存在" : "不存在");
                        if (userInfo != null) {
                            travelNote.setUserName((String) userInfo.get("userName"));
                            travelNote.setNickName((String) userInfo.get("nickName"));
                            travelNote.setAvatar((String) userInfo.get("avatar"));
                            log.info("设置用户信息 - userName: {}, nickName: {}, avatar: {}",
                                    userInfo.get("userName"), userInfo.get("nickName"), userInfo.get("avatar"));
                        } else {
                            log.warn("未找到用户信息: userId={}", travelNote.getUserId());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批量获取用户信息失败: userIds={}", userIds, e);
            }

            // 批量获取目的地信息
            List<String> destinationIds = resultPage.getRecords().stream()
                    .map(TravelNote::getDestinationId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (!destinationIds.isEmpty()) {
                try {
                    Result<?> batchDestinationResult = contentClient.getBatchDestinationDetail(destinationIds);
                    log.info("批量获取目的地信息结果: {}", batchDestinationResult);

                    if (batchDestinationResult != null && batchDestinationResult.getSuccess() && batchDestinationResult.getData() != null) {
                        List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchDestinationResult.getData();
                        log.info("目的地信息列表大小: {}", rawDataList.size());

                        // 创建目的地信息映射
                        Map<String, Map<String, Object>> destinationInfoMap = new java.util.HashMap<>();
                        for (Map<String, Object> destinationData : rawDataList) {
                            String destinationId = (String) destinationData.get("destinationId");
                            if (destinationId == null) {
                                destinationId = (String) destinationData.get("destination_id");
                            }
                            if (destinationId != null) {
                                destinationInfoMap.put(destinationId, destinationData);
                            }
                        }

                        // 为每个游记设置目的地信息
                        for (TravelNote travelNote : resultPage.getRecords()) {
                            Map<String, Object> destinationInfo = destinationInfoMap.get(travelNote.getDestinationId());
                            if (destinationInfo != null) {
                                travelNote.setDestinationName((String) destinationInfo.get("name"));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("批量获取目的地信息失败: destinationIds={}", destinationIds, e);
                }
            }

            // 批量获取点赞、收藏和评论数量
            List<String> noteIds = resultPage.getRecords().stream()
                    .map(TravelNote::getNoteId)
                    .collect(Collectors.toList());

            try {
                // 获取点赞数量
                Result<?> likeCountResult = socialClient.getBatchLikeCount(noteIds);
                if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                    Map<String, Integer> likeCountMap = (Map<String, Integer>) likeCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setLikeCount(likeCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }

                // 获取收藏数量
                Result<?> collectionCountResult = socialClient.getBatchCollectionCount(noteIds);
                if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                    Map<String, Integer> collectionCountMap = (Map<String, Integer>) collectionCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCollectionCount(collectionCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }

                // 获取评论数量
                Result<?> commentCountResult = socialClient.getBatchCommentCount(noteIds);
                if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                    Map<String, Integer> commentCountMap = (Map<String, Integer>) commentCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCommentCount(commentCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }
            } catch (Exception e) {
                log.error("批量获取社交数据失败: noteIds={}", noteIds, e);
            }
        }

        return Result.success(resultPage);
    }

    @Override
    public Result<?> queryMyTravelNoteList(QueryTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();

        log.info("查询我的游记列表 - 参数: noteId={}, userId={}, destinationId={}, title={}, status={}, pageNum={}, pageSize={}",
                dto.getNoteId(), dto.getUserId(), dto.getDestinationId(), dto.getTitle(), dto.getStatus(), dto.getPageNum(), dto.getPageSize());
        log.info("当前用户信息 - currentUid={}", currentUid);

        // 强制只查询当前用户的游记
        dto.setUserId(currentUid);

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getNoteId())) {
            queryWrapper.eq("note_id", dto.getNoteId());
        }
        if (StringUtils.hasText(dto.getUserId())) {
            queryWrapper.eq("user_id", dto.getUserId());
        }
        if (StringUtils.hasText(dto.getDestinationId())) {
            queryWrapper.eq("destination_id", dto.getDestinationId());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            queryWrapper.like("title", dto.getTitle());
        }
        // 如果指定了状态，则按状态过滤；否则显示所有状态（包括草稿和已驳回）
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
            log.info("按状态过滤游记: status={}", dto.getStatus());
        } else {
            log.info("显示所有状态的游记（包括草稿和已驳回）");
        }
        queryWrapper.orderByDesc("created_at");
        Page<TravelNote> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<TravelNote> resultPage = page(page, queryWrapper);

        log.info("查询结果 - 总数: {}, 当前页记录数: {}", resultPage.getTotal(), resultPage.getRecords().size());

        // 批量获取用户信息
        if (!resultPage.getRecords().isEmpty()) {
            List<String> userIds = resultPage.getRecords().stream()
                    .map(TravelNote::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            try {
                Result<?> batchUserInfoResult = usersClient.getBatchUserInfo(userIds);
                if (batchUserInfoResult != null && batchUserInfoResult.getSuccess() && batchUserInfoResult.getData() != null) {
                    List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchUserInfoResult.getData();
                    Map<String, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
                    for (Map<String, Object> userData : rawDataList) {
                        String uUid = (String) userData.get("uUid");
                        if (uUid == null) {
                            uUid = (String) userData.get("uuid");
                        }
                        if (uUid != null) {
                            userInfoMap.put(uUid, userData);
                        }
                    }

                    for (TravelNote travelNote : resultPage.getRecords()) {
                        Map<String, Object> userInfo = userInfoMap.get(travelNote.getUserId());
                        if (userInfo != null) {
                            travelNote.setUserName((String) userInfo.get("userName"));
                            travelNote.setNickName((String) userInfo.get("nickName"));
                            travelNote.setAvatar((String) userInfo.get("avatar"));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批量获取用户信息失败: userIds={}", userIds, e);
            }

            // 批量获取目的地信息
            List<String> destinationIds = resultPage.getRecords().stream()
                    .map(TravelNote::getDestinationId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (!destinationIds.isEmpty()) {
                try {
                    Result<?> batchDestinationResult = contentClient.getBatchDestinationDetail(destinationIds);
                    if (batchDestinationResult != null && batchDestinationResult.getSuccess() && batchDestinationResult.getData() != null) {
                        List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchDestinationResult.getData();
                        Map<String, Map<String, Object>> destinationInfoMap = new java.util.HashMap<>();
                        for (Map<String, Object> destinationData : rawDataList) {
                            String destinationId = (String) destinationData.get("destinationId");
                            if (destinationId == null) {
                                destinationId = (String) destinationData.get("destination_id");
                            }
                            if (destinationId != null) {
                                destinationInfoMap.put(destinationId, destinationData);
                            }
                        }

                        for (TravelNote travelNote : resultPage.getRecords()) {
                            Map<String, Object> destinationInfo = destinationInfoMap.get(travelNote.getDestinationId());
                            if (destinationInfo != null) {
                                travelNote.setDestinationName((String) destinationInfo.get("name"));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("批量获取目的地信息失败: destinationIds={}", destinationIds, e);
                }
            }

            // 批量获取点赞、收藏和评论数量
            List<String> noteIds = resultPage.getRecords().stream()
                    .map(TravelNote::getNoteId)
                    .collect(Collectors.toList());

            try {
                Result<?> likeCountResult = socialClient.getBatchLikeCount(noteIds);
                if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                    Map<String, Integer> likeCountMap = (Map<String, Integer>) likeCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setLikeCount(likeCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }

                Result<?> collectionCountResult = socialClient.getBatchCollectionCount(noteIds);
                if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                    Map<String, Integer> collectionCountMap = (Map<String, Integer>) collectionCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCollectionCount(collectionCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }

                Result<?> commentCountResult = socialClient.getBatchCommentCount(noteIds);
                if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                    Map<String, Integer> commentCountMap = (Map<String, Integer>) commentCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCommentCount(commentCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }
            } catch (Exception e) {
                log.error("批量获取社交数据失败: noteIds={}", noteIds, e);
            }
        }

        return Result.success(resultPage);
    }

    @Override
    public Result<?> getHotTravelNotes(Integer pageNum, Integer pageSize) {
        log.info("获取热门游记 - pageNum={}, pageSize={}", pageNum, pageSize);

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        // 只查询审核通过的游记
        queryWrapper.eq("status", DictConstants.TravelNoteStatus.PUBLISHED);
        // 按浏览量、点赞数、评论数综合排序（热度 = 浏览量 + 点赞数*10 + 评论数*20）
        queryWrapper.orderByDesc("view_count", "like_count", "comment_count");

        Page<TravelNote> page = new Page<>(pageNum, pageSize);
        Page<TravelNote> resultPage = page(page, queryWrapper);

        log.info("热门游记查询结果 - 总数: {}, 当前页记录数: {}", resultPage.getTotal(), resultPage.getRecords().size());

        // 批量获取用户信息
        if (!resultPage.getRecords().isEmpty()) {
            List<String> userIds = resultPage.getRecords().stream()
                    .map(TravelNote::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            try {
                Result<?> batchUserInfoResult = usersClient.getBatchUserInfo(userIds);
                if (batchUserInfoResult != null && batchUserInfoResult.getSuccess() && batchUserInfoResult.getData() != null) {
                    List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchUserInfoResult.getData();
                    Map<String, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
                    for (Map<String, Object> userData : rawDataList) {
                        String uUid = (String) userData.get("uUid");
                        if (uUid == null) {
                            uUid = (String) userData.get("uuid");
                        }
                        if (uUid != null) {
                            userInfoMap.put(uUid, userData);
                        }
                    }

                    for (TravelNote travelNote : resultPage.getRecords()) {
                        Map<String, Object> userInfo = userInfoMap.get(travelNote.getUserId());
                        if (userInfo != null) {
                            travelNote.setUserName((String) userInfo.get("userName"));
                            travelNote.setNickName((String) userInfo.get("nickName"));
                            travelNote.setAvatar((String) userInfo.get("avatar"));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批量获取用户信息失败: userIds={}", userIds, e);
            }

            // 批量获取目的地信息
            List<String> destinationIds = resultPage.getRecords().stream()
                    .map(TravelNote::getDestinationId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (!destinationIds.isEmpty()) {
                try {
                    Result<?> batchDestinationResult = contentClient.getBatchDestinationDetail(destinationIds);
                    if (batchDestinationResult != null && batchDestinationResult.getSuccess() && batchDestinationResult.getData() != null) {
                        List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchDestinationResult.getData();
                        Map<String, Map<String, Object>> destinationInfoMap = new java.util.HashMap<>();
                        for (Map<String, Object> destinationData : rawDataList) {
                            String destinationId = (String) destinationData.get("destinationId");
                            if (destinationId == null) {
                                destinationId = (String) destinationData.get("destination_id");
                            }
                            if (destinationId != null) {
                                destinationInfoMap.put(destinationId, destinationData);
                            }
                        }

                        for (TravelNote travelNote : resultPage.getRecords()) {
                            Map<String, Object> destinationInfo = destinationInfoMap.get(travelNote.getDestinationId());
                            if (destinationInfo != null) {
                                travelNote.setDestinationName((String) destinationInfo.get("name"));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("批量获取目的地信息失败: destinationIds={}", destinationIds, e);
                }
            }

            // 批量获取点赞、收藏和评论数量
            List<String> noteIds = resultPage.getRecords().stream()
                    .map(TravelNote::getNoteId)
                    .collect(Collectors.toList());

            try {
                Result<?> likeCountResult = socialClient.getBatchLikeCount(noteIds);
                if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                    Map<String, Integer> likeCountMap = (Map<String, Integer>) likeCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setLikeCount(likeCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }

                Result<?> collectionCountResult = socialClient.getBatchCollectionCount(noteIds);
                if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                    Map<String, Integer> collectionCountMap = (Map<String, Integer>) collectionCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCollectionCount(collectionCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }

                Result<?> commentCountResult = socialClient.getBatchCommentCount(noteIds);
                if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                    Map<String, Integer> commentCountMap = (Map<String, Integer>) commentCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCommentCount(commentCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }
            } catch (Exception e) {
                log.error("批量获取社交数据失败: noteIds={}", noteIds, e);
            }
        }

        return Result.success(resultPage);
    }

    @Override
    public Result<?> updateTravelNote(UpdateTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String status = UserContext.getStatus();
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("更新游记操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", dto.getNoteId());
        TravelNote travelNote = getOne(queryWrapper);
        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }
        if (!Objects.equals(travelNote.getUserId(), currentUid)) {
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        BeanUtils.copyProperties(dto, travelNote);
        // 已发布的游记编辑后需要重新审核，改为待审核状态
        if (Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.PUBLISHED)) {
            travelNote.setStatus(DictConstants.TravelNoteStatus.PENDING);
            log.info("用户 {} 编辑已发布的游记 {}, 状态改为待审核", currentUid, dto.getNoteId());
        }
        // 草稿保持草稿，待审核保持待审核，已驳回保持已驳回
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            try {
                travelNote.setImages(objectMapper.writeValueAsString(dto.getImages()));
            } catch (JsonProcessingException e) {
                log.error("图片列表序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "图片列表格式错误");
            }
        }
        boolean success = updateById(travelNote);
        return success ? Result.success("更新成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新失败");
    }

    @Override
    public Result<TravelNoteDetailVO> getMyTravelNoteDetail(GetTravelNoteDetailDTO dto) {
        String currentUid = UserContext.getUserUUid();

        if (currentUid == null) {
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "请先登录");
        }

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", dto.getNoteId());
        TravelNote travelNote = getOne(queryWrapper);
        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        // 检查权限：只能查看自己的游记
        if (!Objects.equals(travelNote.getUserId(), currentUid)) {
            log.info("查看游记失败：用户无权限，noteId={}, currentUid={}, authorId={}",
                dto.getNoteId(), currentUid, travelNote.getUserId());
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有权限查看此游记");
        }

        // 增加浏览数（草稿不增加浏览数）
        if (!Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.DRAFT)) {
            incrementViewCount(dto.getNoteId());
        }

        TravelNoteDetailVO vo = new TravelNoteDetailVO();
        BeanUtils.copyProperties(travelNote, vo);

        // 查询用户信息
        if (StringUtils.hasText(travelNote.getUserId())) {
            try {
                Result<?> userInfoResult = usersClient.getUserInfo(travelNote.getUserId());
                if (userInfoResult != null && userInfoResult.getData() != null) {
                    Map<String, Object> userInfo = (Map<String, Object>) userInfoResult.getData();
                    vo.setUserName((String) userInfo.get("userName"));
                    vo.setNickName((String) userInfo.get("nickName"));
                    vo.setUserAvatar((String) userInfo.get("avatar"));
                }
            } catch (Exception e) {
                log.error("获取用户信息失败: userId={}", travelNote.getUserId(), e);
            }
        }

        // 处理图片
        if (StringUtils.hasText(travelNote.getImages())) {
            try {
                List<String> imageList = objectMapper.readValue(travelNote.getImages(), new TypeReference<List<String>>() {});
                vo.setImages(imageList);
            } catch (Exception e) {
                log.error("解析图片列表失败", e);
            }
        }

        // 获取目的地信息
        if (StringUtils.hasText(travelNote.getDestinationId())) {
            QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
            destinationQuery.eq("destination_id", travelNote.getDestinationId());
            Destination destination = destinationMapper.selectOne(destinationQuery);
            if (destination != null) {
                vo.setDestinationName(destination.getName());
            }
        }

        // 获取点赞数
        try {
            Result<?> likeCountResult = socialClient.getLikeCount("travel_note", travelNote.getNoteId());
            if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                vo.setLikeCount((Integer) likeCountResult.getData());
            }
        } catch (Exception e) {
            log.error("获取点赞数失败: noteId={}", travelNote.getNoteId(), e);
            vo.setLikeCount(0);
        }

        // 获取评论数
        try {
            Result<?> commentCountResult = socialClient.getCommentCount(travelNote.getNoteId());
            if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                vo.setCommentCount((Integer) commentCountResult.getData());
            }
        } catch (Exception e) {
            log.error("获取评论数失败: noteId={}", travelNote.getNoteId(), e);
            vo.setCommentCount(0);
        }

        // 获取收藏数
        try {
            Result<?> collectionCountResult = socialClient.getCollectionCount(travelNote.getNoteId());
            if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                vo.setCollectionCount((Integer) collectionCountResult.getData());
            }
        } catch (Exception e) {
            log.error("获取收藏数失败: noteId={}", travelNote.getNoteId(), e);
            vo.setCollectionCount(0);
        }

        return Result.success(vo);
    }

    @Override
    public Result<TravelNoteDetailVO> getTravelNoteDetail(GetTravelNoteDetailDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", dto.getNoteId());
        TravelNote travelNote = getOne(queryWrapper);
        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        // 检查权限：草稿只能被作者查看
        if (Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.DRAFT)) {
            if (currentUid == null) {
                log.info("查看草稿失败：用户未登录，noteId={}", dto.getNoteId());
                return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "请先登录后查看草稿");
            }
            if (!Objects.equals(travelNote.getUserId(), currentUid)) {
                log.info("查看草稿失败：用户无权限，noteId={}, currentUid={}, authorId={}",
                    dto.getNoteId(), currentUid, travelNote.getUserId());
                return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有权限查看此草稿");
            }
        }

        // 检查权限：已驳回的游记只能被作者查看
        if (Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.REJECTED)) {
            if (currentUid == null) {
                log.info("查看已驳回游记失败：用户未登录，noteId={}", dto.getNoteId());
                return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "该游记已被驳回，无法查看");
            }
            if (!Objects.equals(travelNote.getUserId(), currentUid)) {
                log.info("查看已驳回游记失败：用户无权限，noteId={}, currentUid={}, authorId={}",
                    dto.getNoteId(), currentUid, travelNote.getUserId());
                return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "该游记已被驳回，无法查看");
            }
        }

        // 增加浏览数（草稿不增加浏览数）
        if (!Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.DRAFT)) {
            incrementViewCount(dto.getNoteId());
        }

        TravelNoteDetailVO vo = new TravelNoteDetailVO();
        BeanUtils.copyProperties(travelNote, vo);

        // 查询用户信息
        if (StringUtils.hasText(travelNote.getUserId())) {
            try {
                Result<?> userInfoResult = usersClient.getUserInfo(travelNote.getUserId());
                if (userInfoResult != null && userInfoResult.getData() != null) {
                    Map<String, Object> userInfo = (Map<String, Object>) userInfoResult.getData();
                    vo.setUserName((String) userInfo.get("userName"));
                    vo.setNickName((String) userInfo.get("nickName"));
                    vo.setUserAvatar((String) userInfo.get("avatar"));
                }
            } catch (Exception e) {
                log.error("获取用户信息失败: userId={}", travelNote.getUserId(), e);
            }
        }

        // 解析图片列表
        if (StringUtils.hasText(travelNote.getImages())) {
            try {
                List<String> images = objectMapper.readValue(travelNote.getImages(), new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (JsonProcessingException e) {
                log.error("图片列表解析失败", e);
            }
        }

        // 查询目的地名称
        if (StringUtils.hasText(travelNote.getDestinationId())) {
            QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
            destinationQuery.eq("destination_id", travelNote.getDestinationId());
            Destination destination = destinationMapper.selectOne(destinationQuery);
            if (destination != null) {
                vo.setDestinationName(destination.getName());
            }
        }

        // 获取点赞数
        try {
            Result<?> likeCountResult = socialClient.getLikeCount("travel_note", travelNote.getNoteId());
            if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                vo.setLikeCount((Integer) likeCountResult.getData());
            }
        } catch (Exception e) {
            log.error("获取点赞数失败: noteId={}", travelNote.getNoteId(), e);
            vo.setLikeCount(0);
        }

        // 获取评论数
        try {
            Result<?> commentCountResult = socialClient.getCommentCount(travelNote.getNoteId());
            if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                vo.setCommentCount((Integer) commentCountResult.getData());
            }
        } catch (Exception e) {
            log.error("获取评论数失败: noteId={}", travelNote.getNoteId(), e);
            vo.setCommentCount(0);
        }

        // 获取收藏数
        try {
            Result<?> collectionCountResult = socialClient.getCollectionCount(travelNote.getNoteId());
            if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                vo.setCollectionCount((Integer) collectionCountResult.getData());
            }
        } catch (Exception e) {
            log.error("获取收藏数失败: noteId={}", travelNote.getNoteId(), e);
            vo.setCollectionCount(0);
        }

        return Result.success(vo);
    }

    @Override
    public Result<?> auditTravelNote(AuditTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();

        // 验证管理员权限
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("审核游记操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }

        // 验证账户状态
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("审核游记操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 验证审核状态
        if (dto.getStatus() != 0 && dto.getStatus() != 1 && dto.getStatus() != 2) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "审核状态无效");
        }

        // 批量更新游记状态
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("note_id", dto.getNoteIds());
        List<TravelNote> travelNotes = list(queryWrapper);

        if (travelNotes.isEmpty()) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        for (TravelNote travelNote : travelNotes) {
            travelNote.setStatus(dto.getStatus());
            updateById(travelNote);
        }

        String action = dto.getStatus() == 1 ? "通过" : (dto.getStatus() == 2 ? "驳回" : "待审核");
        return Result.success("审核" + action + "成功");
    }

    @Override
    public Result<?> setTopTravelNote(SetTopTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();

        // 验证管理员权限
        if (!Objects.equals(role, DictConstants.UserRole.USER)) {
            log.info("置顶游记操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }

        // 验证账户状态
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("置顶游记操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 查询游记
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", dto.getNoteId());
        TravelNote travelNote = getOne(queryWrapper);

        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        // 验证是否是游记作者
        if (!Objects.equals(currentUid, travelNote.getUserId())) {
            log.info("置顶游记操作者：{} 不是游记作者，游记作者：{}", currentUid, travelNote.getUserId());
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "只有作者才能置顶自己的游记");
        }

        // 验证游记状态：只有已审核通过的游记才能被置顶
        if (dto.getIsTop() && !Objects.equals(travelNote.getStatus(), DictConstants.TravelNoteStatus.PUBLISHED)) {
            log.info("置顶游记操作者：{} 尝试置顶状态为{}的游记", currentUid, travelNote.getStatus());
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "只有已审核通过的游记才能被置顶");
        }

        // 设置置顶状态
        if (dto.getIsTop()) {
            // 置顶：设置较大的排序值
            travelNote.setSortOrder(9999);
        } else {
            // 取消置顶：恢复默认排序值
            travelNote.setSortOrder(0);
        }

        boolean success = updateById(travelNote);
        String action = dto.getIsTop() ? "置顶" : "取消置顶";
        return success ? Result.success(action + "成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), action + "失败");
    }

    @Override
    public Result<?> getBatchTravelNoteDetail(List<String> noteIds) {
        if (noteIds == null || noteIds.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "游记ID列表不能为空");
        }
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("note_id", noteIds);
        // 只返回审核通过的游记
        queryWrapper.eq("status", DictConstants.TravelNoteStatus.PUBLISHED);
        List<TravelNote> travelNotes = list(queryWrapper);

        // 批量获取用户信息
        if (!travelNotes.isEmpty()) {
            List<String> userIds = travelNotes.stream()
                    .map(TravelNote::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            try {
                log.info("开始批量获取用户信息，userIds={}", userIds);
                Result<?> batchUserInfoResult = usersClient.getBatchUserInfo(userIds);
                log.info("批量获取用户信息返回结果: success={}, data={}",
                        batchUserInfoResult != null ? batchUserInfoResult.getSuccess() : null,
                        batchUserInfoResult != null ? batchUserInfoResult.getData() : null);

                if (batchUserInfoResult != null && batchUserInfoResult.getSuccess() && batchUserInfoResult.getData() != null) {
                    List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchUserInfoResult.getData();
                    log.info("获取到用户信息列表，数量: {}", rawDataList.size());

                    Map<String, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
                    for (Map<String, Object> userData : rawDataList) {
                        // 兼容两种字段名：uUid 和 uuid
                        String uUid = (String) userData.get("uUid");
                        if (uUid == null) {
                            uUid = (String) userData.get("uuid");
                        }
                        if (uUid != null) {
                            userInfoMap.put(uUid, userData);
                            log.info("用户信息: uUid={}, userName={}, nickName={}, avatar={}",
                                    uUid, userData.get("userName"), userData.get("nickName"), userData.get("avatar"));
                        }
                    }

                    // 为每个游记设置用户信息
                    for (TravelNote travelNote : travelNotes) {
                        Map<String, Object> userInfo = userInfoMap.get(travelNote.getUserId());
                        if (userInfo != null) {
                            String userName = (String) userInfo.get("userName");
                            String nickName = (String) userInfo.get("nickName");
                            String avatar = (String) userInfo.get("avatar");

                            // 如果 nickName 为空，则使用 userName
                            if (nickName == null || nickName.isEmpty()) {
                                nickName = userName;
                            }

                            travelNote.setUserName(userName);
                            travelNote.setNickName(nickName);
                            travelNote.setAvatar(avatar);
                            log.info("设置游记用户信息: noteId={}, userId={}, userName={}, nickName={}, avatar={}",
                                    travelNote.getNoteId(), travelNote.getUserId(),
                                    userName, nickName, avatar);
                        } else {
                            log.warn("未找到用户信息: userId={}", travelNote.getUserId());
                        }
                    }

                    // 打印所有游记对象，用于调试
                    log.info("所有游记对象: {}", travelNotes);
                    for (TravelNote travelNote : travelNotes) {
                        log.info("游记详情: noteId={}, userId={}, userName={}, nickName={}, avatar={}",
                                travelNote.getNoteId(), travelNote.getUserId(),
                                travelNote.getUserName(), travelNote.getNickName(), travelNote.getAvatar());
                    }
                }
            } catch (Exception e) {
                log.error("批量获取用户信息失败: userIds={}", userIds, e);
            }
        }

        // 获取点赞数量
        try {
            Result<?> likeCountResult = socialClient.getBatchLikeCount(noteIds);
            if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                Map<String, Integer> likeCountMap = (Map<String, Integer>) likeCountResult.getData();
                for (TravelNote travelNote : travelNotes) {
                    travelNote.setLikeCount(likeCountMap.getOrDefault(travelNote.getNoteId(), 0));
                }
            }
        } catch (Exception e) {
            log.error("获取点赞数失败: noteIds={}", noteIds, e);
            for (TravelNote travelNote : travelNotes) {
                travelNote.setLikeCount(0);
            }
        }

        // 获取评论数量
        try {
            Result<?> commentCountResult = socialClient.getBatchCommentCount(noteIds);
            if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                Map<String, Integer> commentCountMap = (Map<String, Integer>) commentCountResult.getData();
                for (TravelNote travelNote : travelNotes) {
                    travelNote.setCommentCount(commentCountMap.getOrDefault(travelNote.getNoteId(), 0));
                }
            }
        } catch (Exception e) {
            log.error("获取评论数失败: noteIds={}", noteIds, e);
            for (TravelNote travelNote : travelNotes) {
                travelNote.setCommentCount(0);
            }
        }

        // 获取收藏数量
        try {
            Result<?> collectionCountResult = socialClient.getBatchCollectionCount(noteIds);
            if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                Map<String, Integer> collectionCountMap = (Map<String, Integer>) collectionCountResult.getData();
                for (TravelNote travelNote : travelNotes) {
                    travelNote.setCollectionCount(collectionCountMap.getOrDefault(travelNote.getNoteId(), 0));
                }
            }
        } catch (Exception e) {
            log.error("获取收藏数失败: noteIds={}", noteIds, e);
            for (TravelNote travelNote : travelNotes) {
                travelNote.setCollectionCount(0);
            }
        }

        return Result.success(travelNotes);
    }

    @Override
    public Result<?> incrementViewCount(String noteId) {
        if (!StringUtils.hasText(noteId)) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "游记ID不能为空");
        }

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", noteId);
        TravelNote travelNote = getOne(queryWrapper);

        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
        }

        // 增加浏览数
        travelNote.setViewCount((travelNote.getViewCount() == null ? 0 : travelNote.getViewCount()) + 1);
        boolean success = updateById(travelNote);

        return success ? Result.success("浏览数增加成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "浏览数增加失败");
    }

    @Override
    public Result<?> queryTravelNoteListForAdmin(QueryTravelNoteDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();

        log.info("管理员查询游记列表 - 参数: noteId={}, userId={}, destinationId={}, title={}, status={}, pageNum={}, pageSize={}",
                dto.getNoteId(), dto.getUserId(), dto.getDestinationId(), dto.getTitle(), dto.getStatus(), dto.getPageNum(), dto.getPageSize());
        log.info("当前用户信息 - currentUid={}, role={}", currentUid, role);

        // 验证是否是管理员
        if (!DictConstants.UserRole.ADMIN.equals(role)) {
            return Result.error(ResultCode.FORBIDDEN.getCode(), "只有管理员才能使用此接口");
        }

        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getNoteId())) {
            queryWrapper.eq("note_id", dto.getNoteId());
        }
        if (StringUtils.hasText(dto.getUserId())) {
            queryWrapper.eq("user_id", dto.getUserId());
        }
        if (StringUtils.hasText(dto.getDestinationId())) {
            queryWrapper.eq("destination_id", dto.getDestinationId());
        }
        if (StringUtils.hasText(dto.getTitle())) {
            queryWrapper.like("title", dto.getTitle());
        }
        // 管理员可以按状态过滤，如果不指定 status，则显示所有状态
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        // 不添加默认的 status 过滤，管理员可以看到所有状态的游记
        queryWrapper.orderByDesc("created_at");

        // 分页查询
        Page<TravelNote> page = new Page<>(dto.getPageNum() != null ? dto.getPageNum() : 1,
                dto.getPageSize() != null ? dto.getPageSize() : 10);
        Page<TravelNote> resultPage = this.page(page, queryWrapper);

        log.info("查询结果 - 总数: {}, 当前页记录数: {}", resultPage.getTotal(), resultPage.getRecords().size());

        // 批量获取用户信息
        if (!resultPage.getRecords().isEmpty()) {
            List<String> userIds = resultPage.getRecords().stream()
                    .map(TravelNote::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            log.info("游记列表查询到的用户ID列表: {}", userIds);

            try {
                Result<?> batchUserInfoResult = usersClient.getBatchUserInfo(userIds);
                log.info("批量获取用户信息结果: {}", batchUserInfoResult);

                if (batchUserInfoResult != null && batchUserInfoResult.getSuccess() && batchUserInfoResult.getData() != null) {
                    List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchUserInfoResult.getData();
                    log.info("用户信息列表大小: {}", rawDataList.size());

                    // 创建用户信息映射
                    Map<String, Map<String, Object>> userInfoMap = new java.util.HashMap<>();
                    for (Map<String, Object> userData : rawDataList) {
                        // 兼容两种字段名：uUid 和 uuid
                        String uUid = (String) userData.get("uUid");
                        if (uUid == null) {
                            uUid = (String) userData.get("uuid");
                        }
                        if (uUid != null) {
                            userInfoMap.put(uUid, userData);
                        }
                    }

                    // 为每个游记设置用户信息
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        Map<String, Object> userInfo = userInfoMap.get(travelNote.getUserId());
                        if (userInfo != null) {
                            travelNote.setUserName((String) userInfo.get("userName"));
                            travelNote.setNickName((String) userInfo.get("nickName"));
                            travelNote.setAvatar((String) userInfo.get("avatar"));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批量获取用户信息失败: userIds={}", userIds, e);
            }

            // 批量获取目的地信息
            List<String> destinationIds = resultPage.getRecords().stream()
                    .map(TravelNote::getDestinationId)
                    .filter(id -> id != null && !id.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (!destinationIds.isEmpty()) {
                try {
                    Result<?> batchDestinationResult = contentClient.getBatchDestinationDetail(destinationIds);
                    log.info("批量获取目的地信息结果: {}", batchDestinationResult);

                    if (batchDestinationResult != null && batchDestinationResult.getSuccess() && batchDestinationResult.getData() != null) {
                        List<Map<String, Object>> rawDataList = (List<Map<String, Object>>) batchDestinationResult.getData();
                        log.info("目的地信息列表大小: {}", rawDataList.size());

                        // 创建目的地信息映射
                        Map<String, Map<String, Object>> destinationInfoMap = new java.util.HashMap<>();
                        for (Map<String, Object> destinationData : rawDataList) {
                            String destinationId = (String) destinationData.get("destinationId");
                            if (destinationId == null) {
                                destinationId = (String) destinationData.get("destination_id");
                            }
                            if (destinationId != null) {
                                destinationInfoMap.put(destinationId, destinationData);
                            }
                        }

                        // 为每个游记设置目的地信息
                        for (TravelNote travelNote : resultPage.getRecords()) {
                            Map<String, Object> destinationInfo = destinationInfoMap.get(travelNote.getDestinationId());
                            if (destinationInfo != null) {
                                travelNote.setDestinationName((String) destinationInfo.get("name"));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("批量获取目的地信息失败: destinationIds={}", destinationIds, e);
                }
            }

            // 批量获取点赞数
            List<String> noteIds = resultPage.getRecords().stream()
                    .map(TravelNote::getNoteId)
                    .collect(Collectors.toList());

            try {
                Result<?> likeCountResult = socialClient.getBatchLikeCount(noteIds);
                if (likeCountResult != null && likeCountResult.getSuccess() && likeCountResult.getData() != null) {
                    Map<String, Integer> likeCountMap = (Map<String, Integer>) likeCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setLikeCount(likeCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }
            } catch (Exception e) {
                log.error("获取点赞数失败: noteIds={}", noteIds, e);
                for (TravelNote travelNote : resultPage.getRecords()) {
                    travelNote.setLikeCount(0);
                }
            }

            // 批量获取评论数
            try {
                Result<?> commentCountResult = socialClient.getBatchCommentCount(noteIds);
                if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                    Map<String, Integer> commentCountMap = (Map<String, Integer>) commentCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCommentCount(commentCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }
            } catch (Exception e) {
                log.error("获取评论数失败: noteIds={}", noteIds, e);
                for (TravelNote travelNote : resultPage.getRecords()) {
                    travelNote.setCommentCount(0);
                }
            }

            // 批量获取收藏数
            try {
                Result<?> collectionCountResult = socialClient.getBatchCollectionCount(noteIds);
                if (collectionCountResult != null && collectionCountResult.getSuccess() && collectionCountResult.getData() != null) {
                    Map<String, Integer> collectionCountMap = (Map<String, Integer>) collectionCountResult.getData();
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        travelNote.setCollectionCount(collectionCountMap.getOrDefault(travelNote.getNoteId(), 0));
                    }
                }
            } catch (Exception e) {
                log.error("获取收藏数失败: noteIds={}", noteIds, e);
                for (TravelNote travelNote : resultPage.getRecords()) {
                    travelNote.setCollectionCount(0);
                }
            }
        }

        // 返回分页结果
        Map<String, Object> data = new HashMap<>();
        data.put("records", resultPage.getRecords());
        data.put("total", resultPage.getTotal());
        data.put("pageNum", resultPage.getCurrent());
        data.put("pageSize", resultPage.getSize());

        return Result.success(data);
    }
}
