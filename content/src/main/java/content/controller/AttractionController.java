package content.controller;

import common.utils.Result;
import content.pojo.dto.attraction.CreateAttractionDTO;
import content.pojo.dto.attraction.DeleteAttractionDTO;
import content.pojo.dto.attraction.GetAttractionDetailDTO;
import content.pojo.dto.attraction.QueryAttractionDTO;
import content.pojo.dto.attraction.UpdateAttractionDTO;
import content.pojo.vo.AttractionDetailVO;
import content.service.AttractionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/attraction")
public class AttractionController {
    @Autowired
    AttractionService attractionService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建景点")
    public Result<?> createAttraction(@RequestBody @Valid CreateAttractionDTO dto) {
        return attractionService.createAttraction(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除景点")
    public Result<?> deleteAttraction(@RequestBody @Valid DeleteAttractionDTO dto) {
        return attractionService.deleteAttraction(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询景点列表")
    public Result<?> queryAttractionList(@ModelAttribute QueryAttractionDTO dto) {
        return attractionService.queryAttractionList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新景点")
    public Result<?> updateAttraction(@RequestBody @Valid UpdateAttractionDTO dto) {
        return attractionService.updateAttraction(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/detail")
    @Description(value = "获取景点详情")
    public Result<AttractionDetailVO> getAttractionDetail(@ModelAttribute @Valid GetAttractionDetailDTO dto) {
        return attractionService.getAttractionDetail(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/batch-detail")
    @Description(value = "批量获取景点详情")
    public Result<?> getBatchAttractionDetail(@RequestParam("attractionIds") java.util.List<String> attractionIds) {
        return attractionService.getBatchAttractionDetail(attractionIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/increment-view")
    @Description(value = "增加景点浏览数")
    public Result<?> incrementViewCount(@RequestBody @Valid GetAttractionDetailDTO dto) {
        return attractionService.incrementViewCount(dto.getAid());
    }
}
