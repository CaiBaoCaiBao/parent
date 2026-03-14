package content.controller;

import common.utils.Result;
import content.pojo.dto.search.SearchDTO;
import content.pojo.vo.search.SearchResultVO;
import content.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/search")
    @Description(value = "统一搜索接口")
    public Result<?> search(@Valid @ModelAttribute SearchDTO dto) {
        return searchService.search(dto);
    }
}
