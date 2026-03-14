package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.attractiontags.CreateAttractionTagsDTO;
import content.pojo.dto.attractiontags.DeleteAttractionTagsDTO;
import content.pojo.dto.attractiontags.QueryAttractionTagsDTO;
import content.pojo.entity.AttractionTags;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("景点标签关联管理")
public interface AttractionTagsService extends IService<AttractionTags> {
    @Description("创建景点标签关联（管理员）")
    Result<?> createAttractionTags(@Valid CreateAttractionTagsDTO dto);

    @Description("批量删除景点标签关联（管理员）")
    Result<?> deleteAttractionTags(@Valid DeleteAttractionTagsDTO dto);

    @Description("查询景点标签关联列表")
    Result<?> queryAttractionTagsList(QueryAttractionTagsDTO dto);
}
