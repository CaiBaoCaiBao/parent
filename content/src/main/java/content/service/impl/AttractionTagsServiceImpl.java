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
import content.pojo.dto.attractiontags.CreateAttractionTagsDTO;
import content.pojo.dto.attractiontags.DeleteAttractionTagsDTO;
import content.pojo.dto.attractiontags.QueryAttractionTagsDTO;
import content.pojo.entity.Attraction;
import content.pojo.entity.AttractionTags;
import content.service.AttractionTagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
public class AttractionTagsServiceImpl extends ServiceImpl<AttractionTagsMapper, AttractionTags> implements AttractionTagsService {
    @Autowired
    AttractionTagsMapper attractionTagsMapper;
    @Autowired
    AttractionMapper attractionMapper;

    @Override
    public Result<?> createAttractionTags(CreateAttractionTagsDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建景点标签关联操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建景点标签关联操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
        attractionQuery.eq("aid", dto.getAttractionId());
        Attraction attraction = attractionMapper.selectOne(attractionQuery);
        if (attraction == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "景点不存在");
        }
        AttractionTags attractionTags = new AttractionTags();
        BeanUtils.copyProperties(dto, attractionTags);
        attractionTags.setAttractionId(attraction.getId());
        boolean success = save(attractionTags);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deleteAttractionTags(DeleteAttractionTagsDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除景点标签关联操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除景点标签关联操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<AttractionTags> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", dto.getIds());
        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryAttractionTagsList(QueryAttractionTagsDTO dto) {
        QueryWrapper<AttractionTags> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getAttractionId())) {
            QueryWrapper<Attraction> attractionQuery = new QueryWrapper<>();
            attractionQuery.eq("aid", dto.getAttractionId());
            Attraction attraction = attractionMapper.selectOne(attractionQuery);
            if (attraction != null) {
                queryWrapper.eq("attraction_id", attraction.getId());
            }
        }
        if (dto.getTagId() != null) {
            queryWrapper.eq("tag_id", dto.getTagId());
        }
        if (dto.getRecommendFlag() != null) {
            queryWrapper.eq("recommend_flag", dto.getRecommendFlag());
        }
        queryWrapper.orderByDesc("weight");
        Page<AttractionTags> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<AttractionTags> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }
}
