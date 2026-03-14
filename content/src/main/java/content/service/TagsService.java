package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.tag.CreateTagDTO;
import content.pojo.dto.tag.DeleteTagDTO;
import content.pojo.dto.tag.QueryTagDTO;
import content.pojo.dto.tag.UpdateTagDTO;
import content.pojo.entity.Tags;
import jakarta.validation.Valid;

public interface TagsService extends IService<Tags> {

    Result<?> createTag(@Valid CreateTagDTO createTagDTO);

    Result<?> deleteTag(DeleteTagDTO deleteTagDTO);

    Result<?> updateTag(@Valid UpdateTagDTO updateTagDTO);

    Result<?> queryTagList(QueryTagDTO queryTagDTO);
}
