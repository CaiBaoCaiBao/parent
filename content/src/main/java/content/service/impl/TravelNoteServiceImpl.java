package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.client.UsersClient;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import content.mapper.AttractionMapper;
import content.mapper.DestinationMapper;
import content.mapper.TravelNoteMapper;
import content.pojo.dto.travelnote.AuditTravelNoteDTO;
import content.pojo.dto.travelnote.CreateTravelNoteDTO;
import content.pojo.dto.travelnote.DeleteTravelNoteDTO;
import content.pojo.dto.travelnote.GetTravelNoteDetailDTO;
import content.pojo.dto.travelnote.QueryTravelNoteDTO;
import content.pojo.dto.travelnote.SetTopTravelNoteDTO;
import content.pojo.dto.travelnote.UpdateTravelNoteDTO;
import content.pojo.entity.Attraction;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.TravelNoteDetailVO;
import content.service.TravelNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import users.pojo.vo.UserInfoVo;

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
    AttractionMapper attractionMapper;
    @Autowired
    UsersClient usersClient;

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

        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryTravelNoteList(QueryTravelNoteDTO dto) {
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
        }
        queryWrapper.orderByDesc("created_at");
        Page<TravelNote> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<TravelNote> resultPage = page(page, queryWrapper);

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

                if (batchUserInfoResult != null && batchUserInfoResult.getData() != null) {
                    List<UserInfoVo> userInfoList = (List<UserInfoVo>) batchUserInfoResult.getData();
                    log.info("用户信息列表大小: {}", userInfoList.size());

                    if (!userInfoList.isEmpty()) {
                        UserInfoVo firstUser = userInfoList.get(0);
                        log.info("第一个用户信息 - uUid: {}, userName: {}, nickName: {}",
                                firstUser.getUUid(), firstUser.getUserName(), firstUser.getNickName());
                    }

                    // 创建用户信息映射
                    Map<String, UserInfoVo> userInfoMap = userInfoList.stream()
                            .collect(Collectors.toMap(UserInfoVo::getUUid, info -> info));

                    // 为每个游记设置用户信息
                    for (TravelNote travelNote : resultPage.getRecords()) {
                        UserInfoVo userInfo = userInfoMap.get(travelNote.getUserId());
                        log.info("游记ID: {}, 用户ID: {}, 找到的用户信息: {}",
                                travelNote.getNoteId(), travelNote.getUserId(), userInfo != null ? "存在" : "不存在");
                        if (userInfo != null) {
                            travelNote.setUserName(userInfo.getUserName());
                            travelNote.setNickName(userInfo.getNickName());
                            log.info("设置用户信息 - userName: {}, nickName: {}",
                                    userInfo.getUserName(), userInfo.getNickName());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("批量获取用户信息失败: userIds={}", userIds, e);
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
    public Result<TravelNoteDetailVO> getTravelNoteDetail(GetTravelNoteDetailDTO dto) {
        QueryWrapper<TravelNote> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("note_id", dto.getNoteId());
        TravelNote travelNote = getOne(queryWrapper);
        if (travelNote == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游记不存在");
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

        // 查询景点列表
        if (StringUtils.hasText(travelNote.getImages())) {
            try {
                List<String> attractionIds = objectMapper.readValue(travelNote.getImages(), new TypeReference<List<String>>() {});
                if (!attractionIds.isEmpty()) {
                    QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
                    attractionQuery.in("aid", attractionIds);
                    List<Attraction> attractionList = attractionMapper.selectList(attractionQuery);
                    List<TravelNoteDetailVO.AttractionSimpleVO> attractionVOList = attractionList.stream().map(attraction -> {
                        TravelNoteDetailVO.AttractionSimpleVO attractionVO = new TravelNoteDetailVO.AttractionSimpleVO();
                        attractionVO.setId(attraction.getId());
                        attractionVO.setAid(attraction.getAid());
                        attractionVO.setName(attraction.getName());
                        attractionVO.setCoverImg(attraction.getImages() != null && !attraction.getImages().isEmpty() ? attraction.getImages().get(0) : null);
                        attractionVO.setDescription(attraction.getDescription());
                        return attractionVO;
                    }).collect(Collectors.toList());
                    vo.setAttractions(attractionVOList);
                }
            } catch (JsonProcessingException e) {
                log.error("景点ID列表解析失败", e);
            }
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
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
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
        List<TravelNote> travelNotes = list(queryWrapper);
        return Result.success(travelNotes);
    }
}
