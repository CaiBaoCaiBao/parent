package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.destinationtags.CreateDestinationTagsDTO;
import content.pojo.dto.destinationtags.DeleteDestinationTagsDTO;
import content.pojo.dto.destinationtags.QueryDestinationTagsDTO;
import content.pojo.entity.DestinationTags;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("目的地标签关联管理")
public interface DestinationTagsService extends IService<DestinationTags> {
    @Description("创建目的地标签关联（管理员）")
    Result<?> createDestinationTags(@Valid CreateDestinationTagsDTO dto);

    @Description("批量删除目的地标签关联（管理员）")
    Result<?> deleteDestinationTags(@Valid DeleteDestinationTagsDTO dto);

    @Description("查询目的地标签关联列表")
    Result<?> queryDestinationTagsList(QueryDestinationTagsDTO dto);
}
