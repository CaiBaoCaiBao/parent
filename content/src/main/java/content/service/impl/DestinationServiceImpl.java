package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import common.utils.ULIDUtils;
import content.mapper.AttractionMapper;
import content.mapper.DestinationMapper;
import content.mapper.DestinationTagsMapper;
import content.mapper.TagsMapper;
import content.pojo.dto.destination.CreateDestinationDTO;
import content.pojo.dto.destination.DeleteDestinationDTO;
import content.pojo.dto.destination.GetDestinationDetailDTO;
import content.pojo.dto.destination.QueryDestinationDTO;
import content.pojo.dto.destination.UpdateDestinationDTO;
import content.pojo.entity.Attraction;
import content.pojo.entity.Destination;
import content.pojo.entity.DestinationTags;
import content.pojo.entity.Tags;
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
    TagsMapper tagsMapper;
    @Autowired
    AttractionMapper attractionMapper;

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
        if (destination.getStatus() == null || destination.getStatus().isEmpty()) {
            destination.setStatus("1");
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
        QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
        destinationQuery.in("destination_id", dto.getDestinationIds());
        List<Destination> destinationList = list(destinationQuery);
        boolean success = remove(destinationQuery);
        return success ? Result.success("删除成功"):Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(),"删除失败");
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
        if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
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
        tagsQuery.eq("destination_id", destination.getId());
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
}
