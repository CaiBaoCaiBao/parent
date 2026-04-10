package content.service;

import common.utils.Result;
import content.pojo.vo.StatisticsOverviewVO;
import content.pojo.vo.StatisticsTrendVO;
import org.springframework.context.annotation.Description;

import java.util.List;

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

    /**
     * 获取历史统计趋势（最近6个月）
     * @return 历史统计趋势列表
     */
    @Description("获取历史统计趋势")
    Result<List<StatisticsTrendVO>> getStatisticsTrend();

    /**
     * 获取指定时间范围的统计趋势
     * @param startYear 开始年份
     * @param startMonth 开始月份
     * @param endYear 结束年份
     * @param endMonth 结束月份
     * @return 历史统计趋势列表
     */
    @Description("获取指定时间范围的统计趋势")
    Result<List<StatisticsTrendVO>> getStatisticsTrendByRange(int startYear, int startMonth, int endYear, int endMonth);
}
