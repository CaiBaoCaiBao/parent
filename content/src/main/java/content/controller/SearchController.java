package content.controller;

import common.utils.Result;
import content.pojo.dto.search.HotKeywordsDTO;
import content.pojo.dto.search.SearchDTO;
import content.pojo.dto.search.SearchSuggestionDTO;
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

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/suggestions")
    @Description(value = "获取搜索建议")
    public Result<?> getSuggestions(@Valid @ModelAttribute SearchSuggestionDTO dto) {
        return searchService.getSuggestions(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/hot-keywords")
    @Description(value = "获取热门搜索关键词")
    public Result<?> getHotKeywords(@Valid @ModelAttribute HotKeywordsDTO dto) {
        return searchService.getHotKeywords(dto);
    }
}
