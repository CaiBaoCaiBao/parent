package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
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
    AttractionMapper attractionMapper;
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
        // 生成景点ID（使用 "ATTRACTION_" + ULID 格式，使ID更有语义）
        attraction.setAid("ATTRACTION_" + common.utils.ULIDUtils.generateULID());
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
        QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
        attractionQuery.in("aid", dto.getAids());
        boolean success = remove(attractionQuery);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
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
        return Result.success(resultPage);
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
        tagsQuery.eq("attraction_id", attraction.getId());
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

        // 查询门票
        QueryWrapper<Ticket> ticketQuery = new QueryWrapper<>();
        ticketQuery.eq("attraction_id", dto.getAid());
        ticketQuery.orderByAsc("sort_order");
        List<Ticket> ticketList = ticketMapper.selectList(ticketQuery);
        List<AttractionDetailVO.TicketVO> ticketVOList = ticketList.stream().map(ticket -> {
            AttractionDetailVO.TicketVO ticketVO = new AttractionDetailVO.TicketVO();
            BeanUtils.copyProperties(ticket, ticketVO);
            return ticketVO;
        }).collect(Collectors.toList());
        vo.setTickets(ticketVOList);

        // 查询游玩项目
        QueryWrapper<PlayItem> playItemQuery = new QueryWrapper<>();
        playItemQuery.eq("attraction_id", dto.getAid());
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
}
