package content.controller;

import common.utils.Result;
import content.pojo.dto.openhoursexception.CreateOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.DeleteOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.QueryOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.UpdateOpenHoursExceptionDTO;
import content.service.OpenHoursExceptionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open-hours-exception")
public class OpenHoursExceptionController {
    @Autowired
    OpenHoursExceptionService openHoursExceptionService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建例外日期")
    public Result<?> createOpenHoursException(@RequestBody @Valid CreateOpenHoursExceptionDTO dto) {
        return openHoursExceptionService.createOpenHoursException(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除例外日期")
    public Result<?> deleteOpenHoursException(@RequestBody @Valid DeleteOpenHoursExceptionDTO dto) {
        return openHoursExceptionService.deleteOpenHoursException(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询例外日期列表")
    public Result<?> queryOpenHoursExceptionList(@ModelAttribute QueryOpenHoursExceptionDTO dto) {
        return openHoursExceptionService.queryOpenHoursExceptionList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新例外日期")
    public Result<?> updateOpenHoursException(@RequestBody @Valid UpdateOpenHoursExceptionDTO dto) {
        return openHoursExceptionService.updateOpenHoursException(dto);
    }
}
