package content.service;

import common.utils.Result;
import content.pojo.dto.search.HotKeywordsDTO;
import content.pojo.dto.search.SearchDTO;
import content.pojo.dto.search.SearchSuggestionDTO;
import content.pojo.vo.search.SearchResultVO;
import org.springframework.context.annotation.Description;

import java.util.List;

@Description("搜索服务")
public interface SearchService {

    @Description("统一搜索")
    Result<?> search(SearchDTO dto);

    @Description("获取搜索建议")
    Result<?> getSuggestions(SearchSuggestionDTO dto);

    @Description("获取热门搜索关键词")
    Result<?> getHotKeywords(HotKeywordsDTO dto);
}
