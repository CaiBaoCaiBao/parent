package content.controller;

import common.utils.Result;
import content.pojo.dto.playitem.CreatePlayItemDTO;
import content.pojo.dto.playitem.DeletePlayItemDTO;
import content.pojo.dto.playitem.QueryPlayItemDTO;
import content.pojo.dto.playitem.UpdatePlayItemDTO;
import content.service.PlayItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/play-item")
public class PlayItemController {
    @Autowired
    PlayItemService playItemService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建游玩项目")
    public Result<?> createPlayItem(@RequestBody @Valid CreatePlayItemDTO dto) {
        return playItemService.createPlayItem(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除游玩项目")
    public Result<?> deletePlayItem(@RequestBody @Valid DeletePlayItemDTO dto) {
        return playItemService.deletePlayItem(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询游玩项目列表")
    public Result<?> queryPlayItemList(@ModelAttribute QueryPlayItemDTO dto) {
        return playItemService.queryPlayItemList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新游玩项目")
    public Result<?> updatePlayItem(@RequestBody @Valid UpdatePlayItemDTO dto) {
        return playItemService.updatePlayItem(dto);
    }
}
