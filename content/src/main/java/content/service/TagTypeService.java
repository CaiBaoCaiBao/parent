package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.tag.CreateTagTypeDTO;
import content.pojo.dto.tag.DeleteTagTypeDTO;
import content.pojo.dto.tag.QueryTagTypeDTO;
import content.pojo.dto.tag.UpdateTagTypeDTO;
import content.pojo.entity.TagType;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

public interface TagTypeService extends IService<TagType> {
    @Description("创建标签类型")
    Result<?> createTagType(@Valid CreateTagTypeDTO createTagTypeDTO);

    @Description("批量删除标签类型")
    Result<?> deleteTagType(DeleteTagTypeDTO deleteTagTypeDTO);

    @Description("更新标签类型")
    Result<?> updateTagType(@Valid UpdateTagTypeDTO updateTagTypeDTO);

    @Description("查询标签类型列表")
    Result<?> queryTagTypeList(QueryTagTypeDTO queryTagTypeDTO);
}
