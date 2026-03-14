package content.service;

import common.utils.Result;
import content.pojo.dto.search.SearchDTO;
import content.pojo.vo.search.SearchResultVO;
import org.springframework.context.annotation.Description;

@Description("搜索服务")
public interface SearchService {

    @Description("统一搜索")
    Result<?> search(SearchDTO dto);
}
