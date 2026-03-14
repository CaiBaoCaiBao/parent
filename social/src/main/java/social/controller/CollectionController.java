package social.controller;

import common.utils.Result;
import social.pojo.dto.collection.CheckCollectionStatusDTO;
import social.pojo.dto.collection.QueryCollectionListDTO;
import social.pojo.dto.collection.ToggleCollectionDTO;
import social.pojo.vo.CollectionStatusVO;
import social.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collection")
public class CollectionController {
    @Autowired
    CollectionService collectionService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/toggle")
    @Description(value = "收藏/取消收藏")
    public Result<?> toggleCollection(@RequestBody @Valid ToggleCollectionDTO dto) {
        return collectionService.toggleCollection(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/status")
    @Description(value = "检查收藏状态")
    public Result<CollectionStatusVO> checkCollectionStatus(@ModelAttribute @Valid CheckCollectionStatusDTO dto) {
        return collectionService.checkCollectionStatus(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询收藏列表")
    public Result<?> queryCollectionList(@ModelAttribute @Valid QueryCollectionListDTO dto) {
        return collectionService.queryCollectionList(dto);
    }
}
