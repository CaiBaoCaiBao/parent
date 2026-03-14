package content.controller;

import common.utils.Result;
import content.pojo.dto.attractiontags.CreateAttractionTagsDTO;
import content.pojo.dto.attractiontags.DeleteAttractionTagsDTO;
import content.pojo.dto.attractiontags.QueryAttractionTagsDTO;
import content.service.AttractionTagsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attraction-tags")
public class AttractionTagsController {
    @Autowired
    AttractionTagsService attractionTagsService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建景点标签关联")
    public Result<?> createAttractionTags(@RequestBody @Valid CreateAttractionTagsDTO dto) {
        return attractionTagsService.createAttractionTags(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除景点标签关联")
    public Result<?> deleteAttractionTags(@RequestBody @Valid DeleteAttractionTagsDTO dto) {
        return attractionTagsService.deleteAttractionTags(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询景点标签关联列表")
    public Result<?> queryAttractionTagsList(@ModelAttribute QueryAttractionTagsDTO dto) {
        return attractionTagsService.queryAttractionTagsList(dto);
    }
}
