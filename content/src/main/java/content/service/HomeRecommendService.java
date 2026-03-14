package content.service;

import common.utils.Result;
import content.pojo.dto.destination.QueryHotDestinationDTO;
import content.pojo.vo.HomeRecommendVO;
import content.pojo.vo.HotDestinationVO;
import org.springframework.context.annotation.Description;

/**
 * 首页推荐服务
 */
@Description("首页推荐服务")
public interface HomeRecommendService {

    /**
     * 获取首页推荐内容
     * @return 首页推荐内容
     */
    @Description("获取首页推荐内容")
    Result<HomeRecommendVO> getHomeRecommend();

    /**
     * 获取热门目的地排行
     * @param dto 查询参数
     * @return 热门目的地排行
     */
    @Description("获取热门目的地排行")
    Result<HotDestinationVO> getHotDestinationRanking(QueryHotDestinationDTO dto);
}
