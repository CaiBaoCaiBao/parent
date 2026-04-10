package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.PageResult;
import common.utils.Result;
import content.mapper.AttractionMapper;
import content.mapper.AttractionTagsMapper;
import content.mapper.DestinationMapper;
import content.mapper.OpenTimeRuleMapper;
import content.mapper.PlayItemMapper;
import content.mapper.TagsMapper;
import content.mapper.TicketMapper;
import content.pojo.dto.attraction.CreateAttractionDTO;
import content.pojo.dto.attraction.DeleteAttractionDTO;
import content.pojo.dto.attraction.GetAttractionDetailDTO;
import content.pojo.dto.attraction.QueryAttractionDTO;
import content.pojo.dto.attraction.UpdateAttractionDTO;
import content.pojo.entity.Attraction;
import content.pojo.entity.AttractionTags;
import content.pojo.entity.Destination;
import content.pojo.entity.OpenTimeRule;
import content.pojo.entity.PlayItem;
import content.pojo.entity.Tags;
import content.pojo.entity.Ticket;
import content.pojo.vo.AttractionDetailVO;
import content.pojo.vo.AttractionListVO;
import content.service.AttractionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttractionServiceImpl
        extends ServiceImpl<AttractionMapper, Attraction>
        implements AttractionService {
    @Autowired
    AttractionTagsMapper attractionTagsMapper;
    @Autowired
    TagsMapper tagsMapper;
    @Autowired
    TicketMapper ticketMapper;
    @Autowired
    PlayItemMapper playItemMapper;
    @Autowired
    OpenTimeRuleMapper openTimeRuleMapper;
    @Autowired
    DestinationMapper destinationMapper;
    @Autowired
    common.client.SocialClient socialClient;

    @Override
    public Result<?> createAttraction(CreateAttractionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建景点操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建景点操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        Attraction attraction = new Attraction();
        BeanUtils.copyProperties(dto, attraction);

        // 验证经纬度范围（暂时注释）
        // if (dto.getLongitude() != null && (dto.getLongitude().compareTo(new java.math.BigDecimal("-180")) < 0 ||
        //     dto.getLongitude().compareTo(new java.math.BigDecimal("180")) > 0)) {
        //     return Result.error(ResultCode.PARAM_ERROR.getCode(), "经度必须在 -180 到 180 之间");
        // }
        // if (dto.getLatitude() != null && (dto.getLatitude().compareTo(new java.math.BigDecimal("-90")) < 0 ||
        //     dto.getLatitude().compareTo(new java.math.BigDecimal("90")) > 0)) {
        //     return Result.error(ResultCode.PARAM_ERROR.getCode(), "纬度必须在 -90 到 90 之间");
        // }

        // 生成景点ID（使用 "ATTRACTION_" + ULID 格式，使ID更有语义）
        attraction.setAid("ATTRACTION_" + common.utils.ULIDUtils.generateULID());
        // 设置默认状态为启用
        if (attraction.getStatus() == null) {
            attraction.setStatus(1);
        }
        boolean success = save(attraction);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deleteAttraction(DeleteAttractionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除景点操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除景点操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }

        // 查询要删除的景点
        QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
        attractionQuery.in("aid", dto.getAids());
        List<Attraction> attractionList = list(attractionQuery);

        if (attractionList.isEmpty()) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "景点不存在");
        }

        // 级联删除相关数据
        try {
            // 1. 删除景点下的门票
            QueryWrapper<Ticket> ticketQuery = new QueryWrapper<>();
            ticketQuery.in("attraction_id", dto.getAids());
            int deletedTickets = ticketMapper.delete(ticketQuery);
            log.info("删除景点门票成功: aids={}, deletedCount={}", dto.getAids(), deletedTickets);

            // 2. 删除景点下的游玩项目
            QueryWrapper<PlayItem> playItemQuery = new QueryWrapper<>();
            playItemQuery.in("aid", dto.getAids());
            int deletedPlayItems = playItemMapper.delete(playItemQuery);
            log.info("删除景点游玩项目成功: aids={}, deletedCount={}", dto.getAids(), deletedPlayItems);

            // 3. 删除景点下的开放时间规则
            QueryWrapper<OpenTimeRule> openTimeRuleQuery = new QueryWrapper<>();
            openTimeRuleQuery.in("attraction_id", dto.getAids());
            int deletedOpenTimeRules = openTimeRuleMapper.delete(openTimeRuleQuery);
            log.info("删除景点开放时间规则成功: aids={}, deletedCount={}", dto.getAids(), deletedOpenTimeRules);

            // 4. 删除景点标签关联
            QueryWrapper<AttractionTags> attractionTagsQuery = new QueryWrapper<>();
            attractionTagsQuery.in("attraction_id", dto.getAids());
            int deletedAttractionTags = attractionTagsMapper.delete(attractionTagsQuery);
            log.info("删除景点标签关联成功: aids={}, deletedCount={}", dto.getAids(), deletedAttractionTags);

            // 5. 删除景点下的评论（通过SocialClient）
            try {
                Result<?> deleteCommentsResult = socialClient.deleteCommentsByTargetIds("attraction", dto.getAids());
                if (deleteCommentsResult != null && deleteCommentsResult.getSuccess()) {
                    log.info("删除景点评论成功: aids={}", dto.getAids());
                } else {
                    log.warn("删除景点评论失败: aids={}, result={}", dto.getAids(), deleteCommentsResult);
                }
            } catch (Exception e) {
                log.error("删除景点评论异常: aids={}", dto.getAids(), e);
            }

            // 6. 删除景点下的点赞（通过SocialClient）
            try {
                Result<?> deleteLikesResult = socialClient.deleteLikesByTargetIds("attraction", dto.getAids());
                if (deleteLikesResult != null && deleteLikesResult.getSuccess()) {
                    log.info("删除景点点赞成功: aids={}", dto.getAids());
                } else {
                    log.warn("删除景点点赞失败: aids={}, result={}", dto.getAids(), deleteLikesResult);
                }
            } catch (Exception e) {
                log.error("删除景点点赞异常: aids={}", dto.getAids(), e);
            }

            // 7. 删除景点下的收藏（通过SocialClient）
            try {
                Result<?> deleteCollectionsResult = socialClient.deleteCollectionsByTargetIds("attraction", dto.getAids());
                if (deleteCollectionsResult != null && deleteCollectionsResult.getSuccess()) {
                    log.info("删除景点收藏成功: aids={}", dto.getAids());
                } else {
                    log.warn("删除景点收藏失败: aids={}, result={}", dto.getAids(), deleteCollectionsResult);
                }
            } catch (Exception e) {
                log.error("删除景点收藏异常: aids={}", dto.getAids(), e);
            }

            // 8. 删除景点记录
            boolean success = remove(attractionQuery);

            if (success) {
                log.info("删除景点成功: aids={}, operator={}", dto.getAids(), currentUid);
                return Result.success("删除成功");
            } else {
                log.error("删除景点失败: aids={}", dto.getAids());
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
            }
        } catch (Exception e) {
            log.error("删除景点及相关数据失败: aids={}", dto.getAids(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除失败");
        }
    }

    @Override
    public Result<?> queryAttractionList(QueryAttractionDTO dto) {
        QueryWrapper<Attraction> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getAid())) {
            queryWrapper.eq("aid", dto.getAid());
        }
        if (StringUtils.hasText(dto.getDestinationId())) {
            queryWrapper.eq("destination_id", dto.getDestinationId());
        }
        if (StringUtils.hasText(dto.getName())) {
            queryWrapper.like("name", dto.getName());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        queryWrapper.orderByAsc("sort_order");
        Page<Attraction> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<Attraction> resultPage = page(page, queryWrapper);

        // 转换为 VO，排除 deleted 字段
        List<AttractionListVO> voList = resultPage.getRecords().stream().map(attraction -> {
            AttractionListVO vo = new AttractionListVO();
            BeanUtils.copyProperties(attraction, vo);
            // 手动设置 images 字段，确保正确复制
            vo.setImages(attraction.getImages());
            // 调试日志：检查 images 字段
            log.info("景点ID: {}, 名称: {}, images: {}", attraction.getAid(), attraction.getName(), attraction.getImages());
            return vo;
        }).collect(Collectors.toList());

        // 转换为 PageResult 格式
        PageResult<AttractionListVO> pageResult = new PageResult<>(
            voList,
            resultPage.getTotal(),
            (long) resultPage.getCurrent(),
            (long) resultPage.getSize()
        );

        return Result.success(pageResult);
    }

    @Override
    public Result<?> updateAttraction(UpdateAttractionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("更新景点操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("更新景点操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<Attraction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", dto.getAid());
        Attraction attraction = getOne(queryWrapper);
        if (attraction == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "景点不存在");
        }

        // 验证经纬度范围（暂时注释）
        // if (dto.getLongitude() != null && (dto.getLongitude().compareTo(new java.math.BigDecimal("-180")) < 0 ||
        //     dto.getLongitude().compareTo(new java.math.BigDecimal("180")) > 0)) {
        //     return Result.error(ResultCode.PARAM_ERROR.getCode(), "经度必须在 -180 到 180 之间");
        // }
        // if (dto.getLatitude() != null && (dto.getLatitude().compareTo(new java.math.BigDecimal("-90")) < 0 ||
        //     dto.getLatitude().compareTo(new java.math.BigDecimal("90")) > 0)) {
        //     return Result.error(ResultCode.PARAM_ERROR.getCode(), "纬度必须在 -90 到 90 之间");
        // }

        BeanUtils.copyProperties(dto, attraction);
        boolean success = updateById(attraction);
        return success ? Result.success("更新成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新失败");
    }

    @Override
    public Result<AttractionDetailVO> getAttractionDetail(GetAttractionDetailDTO dto) {
        QueryWrapper<Attraction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", dto.getAid());
        Attraction attraction = getOne(queryWrapper);
        if (attraction == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "景点不存在");
        }

        AttractionDetailVO vo = new AttractionDetailVO();
        BeanUtils.copyProperties(attraction, vo);
        // 手动设置 images 字段，确保正确复制
        vo.setImages(attraction.getImages());

        // 查询目的地名称
        if (StringUtils.hasText(attraction.getDestinationId())) {
            QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
            destinationQuery.eq("destination_id", attraction.getDestinationId());
            Destination destination = destinationMapper.selectOne(destinationQuery);
            if (destination != null) {
                vo.setDestinationName(destination.getName());
            }
        }

        // 查询标签
        QueryWrapper<AttractionTags> tagsQuery = new QueryWrapper<>();
        tagsQuery.eq("attraction_id", attraction.getAid());
        List<AttractionTags> attractionTagsList = attractionTagsMapper.selectList(tagsQuery);
        if (!attractionTagsList.isEmpty()) {
            List<Long> tagIds = attractionTagsList.stream()
                    .map(AttractionTags::getTagId)
                    .collect(Collectors.toList());
            QueryWrapper<Tags> tagQuery = new QueryWrapper<>();
            tagQuery.in("id", tagIds);
            List<Tags> tagsList = tagsMapper.selectList(tagQuery);
            List<AttractionDetailVO.TagVO> tagVOList = tagsList.stream().map(tag -> {
                AttractionDetailVO.TagVO tagVO = new AttractionDetailVO.TagVO();
                tagVO.setId(tag.getId());
                tagVO.setTid(tag.getTid());
                tagVO.setTagName(tag.getTagName());
                tagVO.setTagCode(tag.getTagCode());
                tagVO.setIconUrl(tag.getIconUrl());
                tagVO.setColor(tag.getColor());
                AttractionTags at = attractionTagsList.stream()
                        .filter(t -> t.getTagId().equals(tag.getId()))
                        .findFirst()
                        .orElse(null);
                if (at != null) {
                    tagVO.setWeight(at.getWeight());
                    tagVO.setRecommendFlag(at.getRecommendFlag());
                }
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOList);
        }

        // 查询门票（只查询启用状态的）
        QueryWrapper<Ticket> ticketQuery = new QueryWrapper<>();
        ticketQuery.eq("attraction_id", dto.getAid());
        ticketQuery.eq("status", 1);
        ticketQuery.orderByAsc("sort_order");
        List<Ticket> ticketList = ticketMapper.selectList(ticketQuery);
        List<AttractionDetailVO.TicketVO> ticketVOList = ticketList.stream().map(ticket -> {
            AttractionDetailVO.TicketVO ticketVO = new AttractionDetailVO.TicketVO();
            BeanUtils.copyProperties(ticket, ticketVO);
            return ticketVO;
        }).collect(Collectors.toList());
        vo.setTickets(ticketVOList);

        // 查询游玩项目（只查询启用状态的）
        QueryWrapper<PlayItem> playItemQuery = new QueryWrapper<>();
        playItemQuery.eq("aid", dto.getAid());
        playItemQuery.eq("status", 1);
        List<PlayItem> playItemList = playItemMapper.selectList(playItemQuery);
        List<AttractionDetailVO.PlayItemVO> playItemVOList = playItemList.stream().map(playItem -> {
            AttractionDetailVO.PlayItemVO playItemVO = new AttractionDetailVO.PlayItemVO();
            BeanUtils.copyProperties(playItem, playItemVO);
            return playItemVO;
        }).collect(Collectors.toList());
        vo.setPlayItems(playItemVOList);

        // 查询开放时间规则
        QueryWrapper<OpenTimeRule> openTimeRuleQuery = new QueryWrapper<>();
        openTimeRuleQuery.eq("attraction_id", dto.getAid());
        openTimeRuleQuery.orderByAsc("priority");
        List<OpenTimeRule> openTimeRuleList = openTimeRuleMapper.selectList(openTimeRuleQuery);
        List<AttractionDetailVO.OpenTimeRuleVO> openTimeRuleVOList = openTimeRuleList.stream().map(rule -> {
            AttractionDetailVO.OpenTimeRuleVO ruleVO = new AttractionDetailVO.OpenTimeRuleVO();
            BeanUtils.copyProperties(rule, ruleVO);
            if (rule.getStartDate() != null) {
                ruleVO.setStartDate(rule.getStartDate().toString());
            }
            if (rule.getEndDate() != null) {
                ruleVO.setEndDate(rule.getEndDate().toString());
            }
            if (rule.getTimeSlots() != null && !rule.getTimeSlots().isEmpty()) {
                List<AttractionDetailVO.TimeSlotVO> timeSlotVOList = rule.getTimeSlots().stream().map(slot -> {
                    AttractionDetailVO.TimeSlotVO slotVO = new AttractionDetailVO.TimeSlotVO();
                    slotVO.setStart(slot.getStart());
                    slotVO.setEnd(slot.getEnd());
                    return slotVO;
                }).collect(Collectors.toList());
                ruleVO.setTimeSlots(timeSlotVOList);
            }
            return ruleVO;
        }).collect(Collectors.toList());
        vo.setOpenTimeRules(openTimeRuleVOList);

        return Result.success(vo);
    }

    @Override
    public Result<?> getBatchAttractionDetail(java.util.List<String> attractionIds) {
        if (attractionIds == null || attractionIds.isEmpty()) {
            return Result.success(new java.util.ArrayList<>());
        }

        QueryWrapper<Attraction> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("aid", attractionIds);
        List<Attraction> attractionList = list(queryWrapper);

        List<java.util.Map<String, Object>> resultList = attractionList.stream().map(attraction -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("aid", attraction.getAid());
            map.put("name", attraction.getName());
            map.put("images", attraction.getImages());
            map.put("description", attraction.getDescription());
            map.put("address", attraction.getAddress());
            map.put("phone", attraction.getPhone());
//            map.put("longitude", attraction.getLongitude());
//            map.put("latitude", attraction.getLatitude());
            map.put("viewCount", attraction.getViewCount());
            map.put("status", attraction.getStatus());
            map.put("destinationId", attraction.getDestinationId());
            return map;
        }).collect(Collectors.toList());

        return Result.success(resultList);
    }

    @Override
    public Result<?> incrementViewCount(String attractionId) {
        if (attractionId == null || attractionId.trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "景点ID不能为空");
        }

        QueryWrapper<Attraction> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("aid", attractionId);
        Attraction attraction = getOne(queryWrapper);

        if (attraction == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "景点不存在");
        }

        // 增加浏览数
        attraction.setViewCount((attraction.getViewCount() == null ? 0 : attraction.getViewCount()) + 1);
        boolean success = updateById(attraction);

        return success ? Result.success("浏览数增加成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "浏览数增加失败");
    }
}
