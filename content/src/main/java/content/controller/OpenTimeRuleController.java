package content.controller;

import common.utils.Result;
import content.pojo.dto.opentimerule.CreateOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.DeleteOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.QueryOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.UpdateOpenTimeRuleDTO;
import content.service.OpenTimeRuleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open-time-rule")
public class OpenTimeRuleController {
    @Autowired
    OpenTimeRuleService openTimeRuleService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建开放时间规则")
    public Result<?> createOpenTimeRule(@RequestBody @Valid CreateOpenTimeRuleDTO dto) {
        return openTimeRuleService.createOpenTimeRule(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除开放时间规则")
    public Result<?> deleteOpenTimeRule(@RequestBody @Valid DeleteOpenTimeRuleDTO dto) {
        return openTimeRuleService.deleteOpenTimeRule(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询开放时间规则列表")
    public Result<?> queryOpenTimeRuleList(@ModelAttribute QueryOpenTimeRuleDTO dto) {
        return openTimeRuleService.queryOpenTimeRuleList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新开放时间规则")
    public Result<?> updateOpenTimeRule(@RequestBody @Valid UpdateOpenTimeRuleDTO dto) {
        return openTimeRuleService.updateOpenTimeRule(dto);
    }
}
