package content.controller;

import common.utils.Result;
import content.pojo.dto.destination.CreateDestinationDTO;
import content.pojo.dto.destination.DeleteDestinationDTO;
import content.pojo.dto.destination.GetDestinationDetailDTO;
import content.pojo.dto.destination.QueryDestinationDTO;
import content.pojo.dto.destination.UpdateDestinationDTO;
import content.pojo.vo.DestinationDetailVO;
import content.service.DestinationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/destination")
public class DestinationController {
    @Autowired
    DestinationService destinationService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建目的地")
    public Result<?> createDestination(@RequestBody @Valid CreateDestinationDTO dto){
        return destinationService.createDestination(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除目的地")
    public Result<?> deleteDestination(@RequestBody @Valid DeleteDestinationDTO dto){
        return destinationService.deleteDestination(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询目的地列表")
    public Result<?> queryDestinationList(@ModelAttribute QueryDestinationDTO dto){
        return destinationService.queryDestinationList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新目的地")
    public Result<?> updateDestination(@RequestBody @Valid UpdateDestinationDTO dto){
        return destinationService.updateDestination(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/detail")
    @Description(value = "获取目的地详情")
    public Result<DestinationDetailVO> getDestinationDetail(@ModelAttribute @Valid GetDestinationDetailDTO dto){
        return destinationService.getDestinationDetail(dto);
    }
}
