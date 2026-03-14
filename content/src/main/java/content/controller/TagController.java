package content.controller;

import common.utils.Result;
import content.pojo.dto.tag.*;
import content.service.TagTypeService;
import content.service.TagsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Autowired
    TagTypeService tagTypeService;
    @Autowired
    TagsService tagsService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create-type")
    @Description(value = "创建标签类型")
    public Result<?> createTagType(@RequestBody @Valid CreateTagTypeDTO createTagTypeDTO){
        return tagTypeService.createTagType(createTagTypeDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete-type")
    @Description(value = "删除标签类型")
    public Result<?> deleteTagType(@RequestBody DeleteTagTypeDTO deleteTagTypeDTO){
        return tagTypeService.deleteTagType(deleteTagTypeDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update-type")
    @Description(value = "更新标签类型")
    public Result<?> updateTagType(@RequestBody @Valid UpdateTagTypeDTO updateTagTypeDTO){
        return tagTypeService.updateTagType(updateTagTypeDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list-type")
    @Description(value = "查询标签类型列表")
    public Result<?> listTagType(@ModelAttribute QueryTagTypeDTO queryTagTypeDTO){
        return tagTypeService.queryTagTypeList(queryTagTypeDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create-item")
    @Description(value = "创建标签")
    public Result<?> createTag(@RequestBody @Valid CreateTagDTO createTagDTO){
        return tagsService.createTag(createTagDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete-item")
    @Description(value = "删除标签")
    public Result<?> deleteTag(@RequestBody DeleteTagDTO deleteTagDTO){
        return tagsService.deleteTag(deleteTagDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update-item")
    @Description(value = "更新标签")
    public Result<?> updateTag(@RequestBody @Valid UpdateTagDTO updateTagDTO){
        return tagsService.updateTag(updateTagDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list-item")
    @Description(value = "查询标签列表")
    public Result<?> listTag(@ModelAttribute QueryTagDTO queryTagDTO){
        return tagsService.queryTagList(queryTagDTO);
    }

}
