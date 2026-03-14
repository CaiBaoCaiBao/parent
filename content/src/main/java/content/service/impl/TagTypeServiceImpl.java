package content.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import common.utils.ULIDUtils;
import content.mapper.TagTypeMapper;
import content.pojo.dto.tag.CreateTagTypeDTO;
import content.pojo.dto.tag.DeleteTagTypeDTO;
import content.pojo.dto.tag.QueryTagTypeDTO;
import content.pojo.dto.tag.UpdateTagTypeDTO;
import content.pojo.entity.TagType;
import content.pojo.entity.Tags;
import content.service.TagTypeService;
import content.service.TagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagTypeServiceImpl
        extends ServiceImpl<TagTypeMapper, TagType>
        implements TagTypeService {

    @Autowired
    TagTypeMapper tagTypeMapper;
    @Autowired
    TagsService tagsService;


    @Override
    public Result<?> createTagType(CreateTagTypeDTO createTagTypeDTO) {
        TagType tagType = new TagType();
        // 生成标签类型ID（使用 "TAG_TYPE_" + ULID 格式，使ID更有语义）
        tagType.setTagTypeId("TAG_TYPE_" + common.utils.ULIDUtils.generateULID());
        tagType.setTagTypeName(createTagTypeDTO.getTagTypeName());
        tagType.setTagTypeCode(createTagTypeDTO.getTagTypeCode());
        tagType.setIconUrl(createTagTypeDTO.getIconUrl());
        tagType.setStatus(createTagTypeDTO.getStatus());

        int typeFlag = tagTypeMapper.insert(tagType);
        if (typeFlag  != 1) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(),"创建标签类型失败");
        }
        return Result.success("创建标签类型成功");
    }

    @Override
    public Result<?> deleteTagType(DeleteTagTypeDTO deleteTagTypeDTO) {
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        // 如果当前用户不是管理员，则不进行操作
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "无操作权限");
        }
        if (Objects.equals(status, DictConstants.UserStatus.INACTIVE)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }
        List<String> tagTypeIds = deleteTagTypeDTO.getTagTypeIds();
        if (tagTypeIds == null || tagTypeIds.isEmpty()) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "标签类型ID不能为空");
        }

        // 根据 tagTypeIds 查询 TagType 记录
        List<TagType> tagTypes = list(new QueryWrapper<TagType>().in("tag_type_id", tagTypeIds));
        if (tagTypes == null || tagTypes.isEmpty()) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "标签类型不存在");
        }

        // 【修复】获取 TagType 的 tagTypeId 列表（String 类型），用于匹配 Tags.tagTypeId
        List<String> tagTypeIdList = tagTypes.stream()
                .map(TagType::getTagTypeId)
                .collect(Collectors.toList());

        // 【修复】根据 tagTypeId（String）删除关联的 Tags
        boolean removeTags = tagsService.remove(new QueryWrapper<Tags>().in("tag_type_id", tagTypeIdList));
        if (!removeTags) {
            log.warn("删除标签类型关联的标签失败, tagTypeIds: {}", tagTypeIds);
        }

        // 批量删除 TagType
        boolean removeTagTypes = remove(new QueryWrapper<TagType>().in("tag_type_id", tagTypeIds));
        if (!removeTagTypes) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除标签类型失败");
        }

        return Result.success("删除标签类型成功");
    }

    @Override
    public Result<?> updateTagType(UpdateTagTypeDTO updateTagTypeDTO) {
        String tagTypeId = updateTagTypeDTO.getTegTypeId();
        TagType tagType = getOne(new QueryWrapper<TagType>().eq("tag_type_id", tagTypeId));
        if (tagType == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "标签类型不存在");
        }
        tagType.setTagTypeName(updateTagTypeDTO.getTagTypeName());
        tagType.setTagTypeCode(updateTagTypeDTO.getTagTypeCode());
        tagType.setIconUrl(updateTagTypeDTO.getIconUrl());
        tagType.setStatus(updateTagTypeDTO.getStatus());
        boolean updateResult = updateById(tagType);
        if (!updateResult) {
            return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新标签类型失败");
        }
        return Result.success("更新标签类型成功");
    }

    @Override
    public Result<?> queryTagTypeList(QueryTagTypeDTO queryTagTypeDTO) {
        List<TagType> tagTypes = list(new QueryWrapper<TagType>()
                .like(StringUtils.isNotBlank(queryTagTypeDTO.getTagTypeName()), "tag_type_name", queryTagTypeDTO.getTagTypeName())
                .like(StringUtils.isNotBlank(queryTagTypeDTO.getTagTypeCode()), "tag_type_code", queryTagTypeDTO.getTagTypeCode())
                .eq(queryTagTypeDTO.getStatus() != null, "status", queryTagTypeDTO.getStatus()));
        return Result.success(tagTypes);
    }
}
