package content.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.enums.ResultCode;
import common.utils.Result;
import common.utils.ULIDUtils;
import content.mapper.AttractionTagsMapper;
import content.mapper.DestinationTagsMapper;
import content.mapper.TagTypeMapper;
import content.mapper.TagsMapper;
import content.pojo.dto.tag.CreateTagDTO;
import content.pojo.dto.tag.DeleteTagDTO;
import content.pojo.dto.tag.QueryTagDTO;
import content.pojo.dto.tag.UpdateTagDTO;
import content.pojo.entity.AttractionTags;
import content.pojo.entity.DestinationTags;
import content.pojo.entity.TagType;
import content.pojo.entity.Tags;
import content.service.TagTypeService;
import content.service.TagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagsServiceImpl
        extends ServiceImpl<TagsMapper, Tags>
        implements TagsService {
    @Autowired
    TagsMapper tagsMapper;
    @Autowired
    TagTypeMapper tagTypeMapper;
    @Autowired
    DestinationTagsMapper destinationTagsMapper;
    @Autowired
    AttractionTagsMapper attractionTagsMapper;

    @Override
    public Result<?> createTag(CreateTagDTO createTagDTO) {
        // 1. 校验标签类型是否存在
        String tagTypeId = createTagDTO.getTagTypeId();
        if (StringUtils.isBlank(tagTypeId)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "标签类型不能为空");
        }
        QueryWrapper<TagType> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tag_type_id", tagTypeId);
        TagType tagType = tagTypeMapper.selectOne(queryWrapper);
        if (tagType == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "标签类型不存在");
        }

        // 2. 校验 tagCode 是否已存在
        QueryWrapper<Tags> codeWrapper = new QueryWrapper<>();
        codeWrapper.eq("tag_code", createTagDTO.getTagCode());
        Long count = tagsMapper.selectCount(codeWrapper);
        if (count > 0) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "标签代码已存在");
        }

        // 3. 创建标签
        Tags tag = new Tags();
        // 生成标签ID（使用 "TAG_" + ULID 格式，使ID更有语义）
        tag.setTid("TAG_" + common.utils.ULIDUtils.generateULID());
        tag.setTagName(createTagDTO.getTagName());
        tag.setTagCode(createTagDTO.getTagCode());
        tag.setTagTypeId(createTagDTO.getTagTypeId());
        tag.setIconUrl(createTagDTO.getIconUrl());
        tag.setColor(createTagDTO.getColor());
        tag.setStatus(createTagDTO.getStatus() != null ? createTagDTO.getStatus() : 1);
        tag.setSortOrder(createTagDTO.getSortOrder() != null ? createTagDTO.getSortOrder() : 0);

        int insertFlag = tagsMapper.insert(tag);
        if (insertFlag != 1) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建标签失败");
        }
        return Result.success("创建标签成功");
    }

    @Override
    public Result<?> deleteTag(DeleteTagDTO deleteTagDTO) {
        List<String> tagIds = deleteTagDTO.getTagIds();
        if (tagIds == null || tagIds.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "标签ID不能为空");
        }

        // 校验标签是否存在
        QueryWrapper<Tags> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("tid", tagIds);
        List<Tags> tagsList = this.list(queryWrapper);
        if (tagsList == null || tagsList.isEmpty()) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "标签不存在");
        }

        // 【新增】检查外键关联 - 获取标签的 id 列表（Long 类型）
        List<Long> tagIdList = tagsList.stream()
                .map(Tags::getId)
                .collect(Collectors.toList());

        // 检查 AttractionTags 关联
        QueryWrapper<AttractionTags> attractionQuery = new QueryWrapper<>();
        attractionQuery.in("tag_id", tagIdList);
        Long attractionCount = attractionTagsMapper.selectCount(attractionQuery);
        if (attractionCount > 0) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),
                    "该标签已被 " + attractionCount + " 个景点引用，无法删除");
        }

        // 检查 DestinationTags 关联
        QueryWrapper<DestinationTags> destinationQuery = new QueryWrapper<>();
        destinationQuery.in("tag_id", tagIdList);
        Long destinationCount = destinationTagsMapper.selectCount(destinationQuery);
        if (destinationCount > 0) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(),
                    "该标签已被 " + destinationCount + "个目的地引用，无法删除");
        }

        // 批量删除
        boolean removeFlag = remove(queryWrapper);
        if (!removeFlag) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除标签失败");
        }

        return Result.success("删除标签成功");
    }

    @Override
    public Result<?> updateTag(UpdateTagDTO updateTagDTO) {
        String tagId = updateTagDTO.getTagId();
        if (StringUtils.isBlank(tagId)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "标签ID不能为空");
        }

        // 查询标签是否存在
        Tags tag = getOne(new QueryWrapper<Tags>().eq("tid", tagId));
        if (tag == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "标签不存在");
        }

        // 如果更新了 tagCode，校验是否与其他标签重复
        if (StringUtils.isNotBlank(updateTagDTO.getTagCode())
                && !updateTagDTO.getTagCode().equals(tag.getTagCode())) {
            QueryWrapper<Tags> codeWrapper = new QueryWrapper<>();
            codeWrapper.eq("tag_code", updateTagDTO.getTagCode());
            Long count = tagsMapper.selectCount(codeWrapper);
            if (count > 0) {
                return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "标签代码已存在");
            }
            tag.setTagCode(updateTagDTO.getTagCode());
        }

        // 更新字段
        if (StringUtils.isNotBlank(updateTagDTO.getTagName())) {
            tag.setTagName(updateTagDTO.getTagName());
        }
        if (updateTagDTO.getIconUrl() != null) {
            tag.setIconUrl(updateTagDTO.getIconUrl());
        }
        if (updateTagDTO.getColor() != null) {
            tag.setColor(updateTagDTO.getColor());
        }
        if (updateTagDTO.getStatus() != null) {
            tag.setStatus(updateTagDTO.getStatus());
        }
        if (updateTagDTO.getSortOrder() != null) {
            tag.setSortOrder(updateTagDTO.getSortOrder());
        }
        tag.setUpdatedAt(LocalDateTime.now());

        boolean updateFlag = updateById(tag);
        if (!updateFlag) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新标签失败");
        }

        return Result.success("更新标签成功");
    }

    @Override
    public Result<?> queryTagList(QueryTagDTO queryTagDTO) {
        // 构建查询条件
        QueryWrapper<Tags> queryWrapper = new QueryWrapper<>();

        // 按标签类型查询
        if (StringUtils.isNotBlank(queryTagDTO.getTagTypeId())) {
            // tagTypeId 需要转换为 Long，因为 Tags 表中 tagType 是 Long 类型
            try {
                String tagTypeId = queryTagDTO.getTagTypeId();
                queryWrapper.eq("tag_type_id", tagTypeId);
            } catch (NumberFormatException e) {
                log.warn("tagTypeId 格式错误: {}", queryTagDTO.getTagTypeId());
            }
        }

        // 按标签名称模糊查询
        queryWrapper.like(StringUtils.isNotBlank(queryTagDTO.getTagName()), "tag_name", queryTagDTO.getTagName());

        // 按标签代码模糊查询
        queryWrapper.like(StringUtils.isNotBlank(queryTagDTO.getTagCode()), "tag_code", queryTagDTO.getTagCode());

        // 按状态查询
        queryWrapper.eq(queryTagDTO.getStatus() != null, "status", queryTagDTO.getStatus());

        // 按 sortOrder 升序排列
        queryWrapper.orderByAsc("sort_order");
        queryWrapper.orderByDesc("created_at");


        List<Tags> tagsList = this.list(queryWrapper);

        return Result.success(tagsList);
    }
}
