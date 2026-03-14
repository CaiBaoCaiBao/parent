package content.controller;

import common.utils.Result;
import content.pojo.dto.travelnote.AuditTravelNoteDTO;
import content.pojo.dto.travelnote.CreateTravelNoteDTO;
import content.pojo.dto.travelnote.DeleteTravelNoteDTO;
import content.pojo.dto.travelnote.GetTravelNoteDetailDTO;
import content.pojo.dto.travelnote.QueryTravelNoteDTO;
import content.pojo.dto.travelnote.SetTopTravelNoteDTO;
import content.pojo.dto.travelnote.UpdateTravelNoteDTO;
import content.pojo.vo.TravelNoteDetailVO;
import content.service.TravelNoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/travel-note")
public class TravelNoteController {
    @Autowired
    TravelNoteService travelNoteService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建游记")
    public Result<?> createTravelNote(@RequestBody @Valid CreateTravelNoteDTO dto) {
        return travelNoteService.createTravelNote(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除游记")
    public Result<?> deleteTravelNote(@RequestBody @Valid DeleteTravelNoteDTO dto) {
        return travelNoteService.deleteTravelNote(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询游记列表")
    public Result<?> queryTravelNoteList(@ModelAttribute QueryTravelNoteDTO dto) {
        return travelNoteService.queryTravelNoteList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新游记")
    public Result<?> updateTravelNote(@RequestBody @Valid UpdateTravelNoteDTO dto) {
        return travelNoteService.updateTravelNote(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/detail")
    @Description(value = "获取游记详情")
    public Result<TravelNoteDetailVO> getTravelNoteDetail(@ModelAttribute @Valid GetTravelNoteDetailDTO dto) {
        return travelNoteService.getTravelNoteDetail(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/audit")
    @Description(value = "审核游记")
    public Result<?> auditTravelNote(@RequestBody @Valid AuditTravelNoteDTO dto) {
        return travelNoteService.auditTravelNote(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/set-top")
    @Description(value = "置顶/取消置顶游记")
    public Result<?> setTopTravelNote(@RequestBody @Valid SetTopTravelNoteDTO dto) {
        return travelNoteService.setTopTravelNote(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/batch-detail")
    @Description(value = "批量获取游记详情")
    public Result<?> getBatchTravelNoteDetail(@RequestParam("noteIds") List<String> noteIds) {
        return travelNoteService.getBatchTravelNoteDetail(noteIds);
    }
}
