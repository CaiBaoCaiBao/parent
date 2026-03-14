package content.controller;

import common.utils.Result;
import content.pojo.dto.destinationtags.CreateDestinationTagsDTO;
import content.pojo.dto.destinationtags.DeleteDestinationTagsDTO;
import content.pojo.dto.destinationtags.QueryDestinationTagsDTO;
import content.service.DestinationTagsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/destination-tags")
public class DestinationTagsController {
    @Autowired
    DestinationTagsService destinationTagsService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建目的地标签关联")
    public Result<?> createDestinationTags(@RequestBody @Valid CreateDestinationTagsDTO dto) {
        return destinationTagsService.createDestinationTags(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除目的地标签关联")
    public Result<?> deleteDestinationTags(@RequestBody @Valid DeleteDestinationTagsDTO dto) {
        return destinationTagsService.deleteDestinationTags(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询目的地标签关联列表")
    public Result<?> queryDestinationTagsList(@ModelAttribute QueryDestinationTagsDTO dto) {
        return destinationTagsService.queryDestinationTagsList(dto);
    }
}
