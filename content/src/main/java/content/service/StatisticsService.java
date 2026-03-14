package content.service;

import common.utils.Result;
import content.pojo.vo.StatisticsOverviewVO;
import org.springframework.context.annotation.Description;

/**
 * 系统统计服务
 */
@Description("系统统计服务")
public interface StatisticsService {

    /**
     * 获取系统数据概况
     * @return 系统数据概况
     */
    @Description("获取系统数据概况")
    Result<StatisticsOverviewVO> getStatisticsOverview();
}
