package content.controller;

import common.utils.Result;
import content.pojo.vo.StatisticsOverviewVO;
import content.pojo.vo.StatisticsTrendVO;
import content.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统统计控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取系统数据概况
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/overview")
    @Description(value = "获取系统数据概况")
    public Result<StatisticsOverviewVO> getStatisticsOverview() {
        return statisticsService.getStatisticsOverview();
    }

    /**
     * 获取历史统计趋势（最近6个月）
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/trend")
    @Description(value = "获取历史统计趋势")
    public Result<List<StatisticsTrendVO>> getStatisticsTrend() {
        return statisticsService.getStatisticsTrend();
    }

    /**
     * 获取指定时间范围的统计趋势
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/trend/range")
    @Description(value = "获取指定时间范围的统计趋势")
    public Result<List<StatisticsTrendVO>> getStatisticsTrendByRange(
            @RequestParam("startYear") int startYear,
            @RequestParam("startMonth") int startMonth,
            @RequestParam("endYear") int endYear,
            @RequestParam("endMonth") int endMonth) {
        return statisticsService.getStatisticsTrendByRange(startYear, startMonth, endYear, endMonth);
    }
}
