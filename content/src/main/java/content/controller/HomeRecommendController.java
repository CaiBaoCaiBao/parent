package content.controller;

import common.utils.Result;
import content.pojo.dto.destination.QueryHotDestinationDTO;
import content.pojo.vo.HomeRecommendVO;
import content.pojo.vo.HotDestinationVO;
import content.service.HomeRecommendService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 首页推荐控制器
 */
@RestController
@RequestMapping("/home")
public class HomeRecommendController {

    @Autowired
    private HomeRecommendService homeRecommendService;

    /**
     * 获取首页推荐内容
     * 游客和普通用户都可以访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/recommend")
    @Description(value = "获取首页推荐内容")
    public Result<HomeRecommendVO> getHomeRecommend() {
        return homeRecommendService.getHomeRecommend();
    }

    /**
     * 获取热门目的地排行
     * 游客和普通用户都可以访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/hot-destinations")
    @Description(value = "获取热门目的地排行")
    public Result<HotDestinationVO> getHotDestinationRanking(@Valid @ModelAttribute QueryHotDestinationDTO dto) {
        return homeRecommendService.getHotDestinationRanking(dto);
    }
}
