package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import common.utils.ULIDUtils;
import content.mapper.*;
import content.pojo.dto.destination.CreateDestinationDTO;
import content.pojo.dto.destination.DeleteDestinationDTO;
import content.pojo.dto.destination.GetDestinationDetailDTO;
import content.pojo.dto.destination.QueryDestinationDTO;
import content.pojo.dto.destination.UpdateDestinationDTO;
import content.pojo.entity.*;
import content.pojo.vo.DestinationDetailVO;
import content.pojo.vo.DestinationListVo;
import content.service.DestinationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DestinationServiceImpl
        extends ServiceImpl<DestinationMapper, Destination>
        implements DestinationService {
    @Autowired
    DestinationMapper destinationMapper;
    @Autowired
    DestinationTagsMapper destinationTagsMapper;
    @Autowired
    AttractionTagsMapper attractionTagsMapper;
    @Autowired
    TagsMapper tagsMapper;
    @Autowired
    AttractionMapper attractionMapper;
    @Autowired
    content.mapper.TicketMapper ticketMapper;
    @Autowired
    content.mapper.PlayItemMapper playItemMapper;
    @Autowired
    content.mapper.OpenTimeRuleMapper openTimeRuleMapper;
    @Autowired
    common.client.SocialClient socialClient;

    @Override
    public Result<?> createDestination(CreateDestinationDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if(!Objects.equals(role, DictConstants.UserRole.ADMIN)){
            log.info("创建目的地操作者：{} 没有操作权限",currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(),"没有操作权限");
        }
        if(!Objects.equals(status, DictConstants.UserStatus.ACTIVE)){
            log.info("创建目的地操作者：{} 账户处于封禁",currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(),"账户处于封禁，无法进行该操作");
        }
        Destination destination = new Destination();
        BeanUtils.copyProperties(dto, destination);
        // 生成目的地ID（使用 "DEST_" + ULID 格式，使ID更有语义）
        destination.setDestinationId("DEST_" + common.utils.ULIDUtils.generateULID());
        // 设置默认状态为启用
        if (destination.getStatus() == null) {
            destination.setStatus(1);
        }
        boolean success = save(destination);

        return success ? Result.success("创建成功"):Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(),"创建失败");
    }

    @Override
    public Result<?> deleteDestination(DeleteDestinationDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if(!Objects.equals(role, DictConstants.UserRole.ADMIN)){
            log.info("删除目的地操作者：{} 没有操作权限",currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(),"没有操作权限");
        }
        if(!Objects.equals(status, DictConstants.UserStatus.ACTIVE)){
            log.info("删除目的地操作者：{} 账户处于封禁",currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(),"账户处于封禁，无法进行该操作");
        }

        // 查询要删除的目的地
        QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
        destinationQuery.in("destination_id", dto.getDestinationIds());
        List<Destination> destinationList = list(destinationQuery);

        if (destinationList.isEmpty()) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "目的地不存在");
        }

        // 级联删除相关数据
        try {
            // 1. 查询目的地下的所有景点
            QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
            attractionQuery.in("destination_id", dto.getDestinationIds());
            List<Attraction> attractionList = attractionMapper.selectList(attractionQuery);

            if (!attractionList.isEmpty()) {
                List<String> attractionIds = attractionList.stream()
                        .map(Attraction::getAid)
                        .collect(Collectors.toList());

                // 2. 删除景点下的门票
                QueryWrapper<content.pojo.entity.Ticket> ticketQuery = new QueryWrapper<>();
                ticketQuery.in("attraction_id", attractionIds);
                int deletedTickets = ticketMapper.delete(ticketQuery);
                log.info("删除目的地景点门票成功: destinationIds={}, deletedCount={}", dto.getDestinationIds(), deletedTickets);

                // 3. 删除景点下的游玩项目
                QueryWrapper<content.pojo.entity.PlayItem> playItemQuery = new QueryWrapper<>();
                playItemQuery.in("aid", attractionIds);
                int deletedPlayItems = playItemMapper.delete(playItemQuery);
                log.info("删除目的地景点游玩项目成功: destinationIds={}, deletedCount={}", dto.getDestinationIds(), deletedPlayItems);

                // 4. 删除景点下的开放时间规则
                QueryWrapper<content.pojo.entity.OpenTimeRule> openTimeRuleQuery = new QueryWrapper<>();
                openTimeRuleQuery.in("attraction_id", attractionIds);
                int deletedOpenTimeRules = openTimeRuleMapper.delete(openTimeRuleQuery);
                log.info("删除目的地景点开放时间规则成功: destinationIds={}, deletedCount={}", dto.getDestinationIds(), deletedOpenTimeRules);

                // 5. 删除景点标签关联
                QueryWrapper<AttractionTags> attractionTagsQuery = new QueryWrapper<>();
                attractionTagsQuery.in("attraction_id", attractionIds);
                int deletedAttractionTags = attractionTagsMapper.delete(attractionTagsQuery);
                log.info("删除目的地景点标签关联成功: destinationIds={}, deletedCount={}", dto.getDestinationIds(), deletedAttractionTags);

                // 6. 删除景点下的评论（通过SocialClient）
                try {
                    Result<?> deleteCommentsResult = socialClient.deleteCommentsByTargetIds("attraction", attractionIds);
                    if (deleteCommentsResult != null && deleteCommentsResult.getSuccess()) {
                        log.info("删除目的地景点评论成功: attractionIds={}", attractionIds);
                    } else {
                        log.warn("删除目的地景点评论失败: attractionIds={}, result={}", attractionIds, deleteCommentsResult);
                    }
                } catch (Exception e) {
                    log.error("删除目的地景点评论异常: attractionIds={}", attractionIds, e);
                }

                // 7. 删除景点下的点赞（通过SocialClient）
                try {
                    Result<?> deleteLikesResult = socialClient.deleteLikesByTargetIds("attraction", attractionIds);
                    if (deleteLikesResult != null && deleteLikesResult.getSuccess()) {
                        log.info("删除目的地景点点赞成功: attractionIds={}", attractionIds);
                    } else {
                        log.warn("删除目的地景点点赞失败: attractionIds={}, result={}", attractionIds, deleteLikesResult);
                    }
                } catch (Exception e) {
                    log.error("删除目的地景点点赞异常: attractionIds={}", attractionIds, e);
                }

                // 8. 删除景点下的收藏（通过SocialClient）
                try {
                    Result<?> deleteCollectionsResult = socialClient.deleteCollectionsByTargetIds("attraction", attractionIds);
                    if (deleteCollectionsResult != null && deleteCollectionsResult.getSuccess()) {
                        log.info("删除目的地景点收藏成功: attractionIds={}", attractionIds);
                    } else {
                        log.warn("删除目的地景点收藏失败: attractionIds={}, result={}", attractionIds, deleteCollectionsResult);
                    }
                } catch (Exception e) {
                    log.error("删除目的地景点收藏异常: attractionIds={}", attractionIds, e);
                }

                // 9. 删除景点记录
                int deletedAttractions = attractionMapper.delete(attractionQuery);
                log.info("删除目的地景点成功: destinationIds={}, deletedCount={}", dto.getDestinationIds(), deletedAttractions);
            }

            // 10. 删除目的地标签关联
            QueryWrapper<DestinationTags> destinationTagsQuery = new QueryWrapper<>();
            destinationTagsQuery.in("destination_id", dto.getDestinationIds());
            int deletedDestinationTags = destinationTagsMapper.delete(destinationTagsQuery);
            log.info("删除目的地标签关联成功: destinationIds={}, deletedCount={}", dto.getDestinationIds(), deletedDestinationTags);

            // 11. 删除目的地下的评论（通过SocialClient）
            try {
                Result<?> deleteCommentsResult = socialClient.deleteCommentsByTargetIds("destination", dto.getDestinationIds());
                if (deleteCommentsResult != null && deleteCommentsResult.getSuccess()) {
                    log.info("删除目的地评论成功: destinationIds={}", dto.getDestinationIds());
                } else {
                    log.warn("删除目的地评论失败: destinationIds={}, result={}", dto.getDestinationIds(), deleteCommentsResult);
                }
            } catch (Exception e) {
                log.error("删除目的地评论异常: destinationIds={}", dto.getDestinationIds(), e);
            }

            // 12. 删除目的地下的点赞（通过SocialClient）
            try {
                Result<?> deleteLikesResult = socialClient.deleteLikesByTargetIds("destination", dto.getDestinationIds());
                if (deleteLikesResult != null && deleteLikesResult.getSuccess()) {
                    log.info("删除目的地点赞成功: destinationIds={}", dto.getDestinationIds());
                } else {
                    log.warn("删除目的地点赞失败: destinationIds={}, result={}", dto.getDestinationIds(), deleteLikesResult);
                }
            } catch (Exception e) {
                log.error("删除目的地点赞异常: destinationIds={}", dto.getDestinationIds(), e);
            }

            // 13. 删除目的地下的收藏（通过SocialClient）
            try {
                Result<?> deleteCollectionsResult = socialClient.deleteCollectionsByTargetIds("destination", dto.getDestinationIds());
                if (deleteCollectionsResult != null && deleteCollectionsResult.getSuccess()) {
                    log.info("删除目的地收藏成功: destinationIds={}", dto.getDestinationIds());
                } else {
                    log.warn("删除目的地收藏失败: destinationIds={}, result={}", dto.getDestinationIds(), deleteCollectionsResult);
                }
            } catch (Exception e) {
                log.error("删除目的地收藏异常: destinationIds={}", dto.getDestinationIds(), e);
            }

            // 14. 删除目的地记录
            boolean success = remove(destinationQuery);

            if (success) {
                log.info("删除目的地成功: destinationIds={}, operator={}", dto.getDestinationIds(), currentUid);
                return Result.success("删除成功");
            } else {
                log.error("删除目的地失败: destinationIds={}", dto.getDestinationIds());
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
            }
        } catch (Exception e) {
            log.error("删除目的地及相关数据失败: destinationIds={}", dto.getDestinationIds(), e);
            return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "删除失败");
        }
    }

    @Override
    public Result<?> queryDestinationList(QueryDestinationDTO dto) {
        // 构建查询条件
        QueryWrapper<Destination> queryWrapper = new QueryWrapper<>();
        
        // 目的地名称模糊搜索
        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            queryWrapper.like("name", dto.getName().trim());
        }
        
        // 省份
        if (dto.getProvince() != null && !dto.getProvince().trim().isEmpty()) {
            queryWrapper.eq("province", dto.getProvince());
        }
        
        // 城市
        if (dto.getCity() != null && !dto.getCity().trim().isEmpty()) {
            queryWrapper.eq("city", dto.getCity());
        }
        
        // 层级
        if (dto.getLevel() != null) {
            queryWrapper.eq("level", dto.getLevel());
        }
        
        // 状态
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        
        // 排序：按创建时间倒序
        queryWrapper.orderByDesc("created_at");
        
        // 分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Destination> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
            dto.getPageNum() != null ? dto.getPageNum() : 1,
            dto.getPageSize() != null ? dto.getPageSize() : 10
        );
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Destination> resultPage = page(page, queryWrapper);
        
        // 转换为 VO
        List<DestinationListVo> voList = resultPage.getRecords().stream().map(destination -> {
            DestinationListVo vo = new DestinationListVo();
            vo.setDestinationId(destination.getDestinationId());
            vo.setName(destination.getName());
            vo.setAliasesName(destination.getAliasesName());
            vo.setCoverImg(destination.getCoverImg());
            vo.setDescription(destination.getDescription());
            vo.setProvince(destination.getProvince());
            vo.setCity(destination.getCity());
            vo.setLevel(destination.getLevel());
            vo.setBestSeason(destination.getBestSeason());
            vo.setTravelDays(destination.getTravelDays());
            vo.setViewCount(destination.getViewCount());
            vo.setStatus(destination.getStatus());
            vo.setSortOrder(destination.getSortOrder());
            return vo;
        }).collect(Collectors.toList());
        
        // 构建分页结果
        common.utils.PageResult<DestinationListVo> pageResult = new common.utils.PageResult<>(
            voList,
            resultPage.getTotal(),
            (long) resultPage.getCurrent(),
            (long) resultPage.getSize()
        );
        
        return Result.success(pageResult);
    }

    @Override
    public Result<?> updateDestination(UpdateDestinationDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if(!Objects.equals(role, DictConstants.UserRole.ADMIN)){
            log.info("更新目的地操作者：{} 没有操作权限",currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(),"没有操作权限");
        }
        if(!Objects.equals(status, DictConstants.UserStatus.ACTIVE)){
            log.info("更新目的地操作者：{} 账户处于封禁",currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(),"账户处于封禁，无法进行该操作");
        }
        Destination destination = destinationMapper.selectByDestinationId(dto.getDestinationId());
        if (destination == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(),"目的地不存在");
        }
        BeanUtils.copyProperties(dto, destination);
        boolean success = updateById(destination);
        return success ? Result.success("更新成功"):Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(),"更新失败");
    }

    @Override
    public Result<DestinationDetailVO> getDestinationDetail(GetDestinationDetailDTO dto) {
        QueryWrapper<Destination> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("destination_id", dto.getDestinationId());
        Destination destination = getOne(queryWrapper);
        if (destination == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "目的地不存在");
        }

        DestinationDetailVO vo = new DestinationDetailVO();
        BeanUtils.copyProperties(destination, vo);

        // 查询标签
        QueryWrapper<DestinationTags> tagsQuery = new QueryWrapper<>();
        tagsQuery.eq("destination_id", destination.getDestinationId());
        List<DestinationTags> destinationTagsList = destinationTagsMapper.selectList(tagsQuery);
        if (!destinationTagsList.isEmpty()) {
            List<Long> tagIds = destinationTagsList.stream()
                    .map(DestinationTags::getTagId)
                    .collect(Collectors.toList());
            QueryWrapper<Tags> tagQuery = new QueryWrapper<>();
            tagQuery.in("id", tagIds);
            List<Tags> tagsList = tagsMapper.selectList(tagQuery);
            List<DestinationDetailVO.TagVO> tagVOList = tagsList.stream().map(tag -> {
                DestinationDetailVO.TagVO tagVO = new DestinationDetailVO.TagVO();
                tagVO.setId(tag.getId());
                tagVO.setTid(tag.getTid());
                tagVO.setTagName(tag.getTagName());
                tagVO.setTagCode(tag.getTagCode());
                tagVO.setIconUrl(tag.getIconUrl());
                tagVO.setColor(tag.getColor());
                DestinationTags dt = destinationTagsList.stream()
                        .filter(t -> t.getTagId().equals(tag.getId()))
                        .findFirst()
                        .orElse(null);
                if (dt != null) {
                    tagVO.setWeight(dt.getWeight());
                    tagVO.setRecommendFlag(dt.getRecommendFlag());
                }
                return tagVO;
            }).collect(Collectors.toList());
            vo.setTags(tagVOList);
        }

        // 查询景点
        QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
        attractionQuery.eq("destination_id", dto.getDestinationId());
        attractionQuery.last("LIMIT 10");
        List<Attraction> attractionList = attractionMapper.selectList(attractionQuery);
        List<DestinationDetailVO.AttractionSimpleVO> attractionVOList = attractionList.stream().map(attraction -> {
            DestinationDetailVO.AttractionSimpleVO attractionVO = new DestinationDetailVO.AttractionSimpleVO();
            attractionVO.setId(attraction.getId());
            attractionVO.setAid(attraction.getAid());
            attractionVO.setName(attraction.getName());
            attractionVO.setCoverImg(attraction.getImages() != null && !attraction.getImages().isEmpty() ? attraction.getImages().get(0) : null);
            attractionVO.setDescription(attraction.getDescription());
            attractionVO.setViewCount(attraction.getViewCount());
            attractionVO.setSortOrder(attraction.getSortOrder());
            return attractionVO;
        }).collect(Collectors.toList());
        vo.setAttractions(attractionVOList);

        return Result.success(vo);
    }

    @Override
    public Result<?> getBatchDestinationDetail(java.util.List<String> destinationIds) {
        if (destinationIds == null || destinationIds.isEmpty()) {
            return Result.success(new java.util.ArrayList<>());
        }

        QueryWrapper<Destination> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("destination_id", destinationIds);
        List<Destination> destinationList = list(queryWrapper);

        List<java.util.Map<String, Object>> resultList = destinationList.stream().map(destination -> {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("destinationId", destination.getDestinationId());
            map.put("name", destination.getName());
            map.put("coverImg", destination.getCoverImg());
            map.put("description", destination.getDescription());
            map.put("province", destination.getProvince());
            map.put("city", destination.getCity());
            map.put("level", destination.getLevel());
            map.put("bestSeason", destination.getBestSeason());
            map.put("travelDays", destination.getTravelDays());
            map.put("viewCount", destination.getViewCount());
            map.put("status", destination.getStatus());
            return map;
        }).collect(Collectors.toList());

        return Result.success(resultList);
    }

    @Override
    public Result<?> incrementViewCount(String destinationId) {
        if (destinationId == null || destinationId.trim().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "目的地ID不能为空");
        }

        QueryWrapper<Destination> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("destination_id", destinationId);
        Destination destination = getOne(queryWrapper);

        if (destination == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "目的地不存在");
        }

        // 增加浏览数
        destination.setViewCount((destination.getViewCount() == null ? 0 : destination.getViewCount()) + 1);
        boolean success = updateById(destination);

        return success ? Result.success("浏览数增加成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "浏览数增加失败");
    }
}
