package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import common.client.SocialClient;
import common.client.UsersClient;
import common.utils.Result;
import content.pojo.entity.Attraction;
import content.pojo.entity.Destination;
import content.pojo.entity.TravelNote;
import content.pojo.vo.StatisticsOverviewVO;
import content.pojo.vo.StatisticsTrendVO;
import content.service.AttractionService;
import content.service.DestinationService;
import content.service.StatisticsService;
import content.service.TravelNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 系统统计服务实现
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private UsersClient usersClient;

    @Autowired
    private SocialClient socialClient;

    @Autowired
    private TravelNoteService travelNoteService;

    @Autowired
    private DestinationService destinationService;

    @Autowired
    private AttractionService attractionService;

    /**
     * 获取系统数据概况
     */
    @Override
    public Result<StatisticsOverviewVO> getStatisticsOverview() {
        StatisticsOverviewVO vo = new StatisticsOverviewVO();

        // 获取用户总数（通过UsersClient调用users服务）
        try {
            Result<?> userCountResult = usersClient.getUserCount();
            if (userCountResult != null && userCountResult.getSuccess() && userCountResult.getData() != null) {
                Map<String, Object> userData = (Map<String, Object>) userCountResult.getData();
                vo.setTotalUsers(((Number) userData.get("userCount")).longValue());
            } else {
                log.warn("获取用户总数失败，返回结果: {}", userCountResult);
                vo.setTotalUsers(0L);
            }
        } catch (Exception e) {
            log.error("获取用户总数失败", e);
            vo.setTotalUsers(0L);
        }

        // 获取游记总数
        QueryWrapper<TravelNote> travelNoteWrapper = new QueryWrapper<>();
        travelNoteWrapper.eq("deleted", 0);
        vo.setTotalTravelNotes(travelNoteService.count(travelNoteWrapper));

        // 获取评论总数（通过SocialClient调用social服务）
        try {
            Result<?> commentCountResult = socialClient.getCommentCount();
            if (commentCountResult != null && commentCountResult.getSuccess() && commentCountResult.getData() != null) {
                Map<String, Object> commentData = (Map<String, Object>) commentCountResult.getData();
                vo.setTotalComments(((Number) commentData.get("commentCount")).longValue());
            } else {
                log.warn("获取评论总数失败，返回结果: {}", commentCountResult);
                vo.setTotalComments(0L);
            }
        } catch (Exception e) {
            log.error("获取评论总数失败", e);
            vo.setTotalComments(0L);
        }

        // 获取目的地总数
        QueryWrapper<Destination> destinationWrapper = new QueryWrapper<>();
        destinationWrapper.eq("deleted", 0);
        vo.setTotalDestinations(destinationService.count(destinationWrapper));

        // 获取景点总数
        QueryWrapper<Attraction> attractionWrapper = new QueryWrapper<>();
        attractionWrapper.eq("deleted", 0);
        vo.setTotalAttractions(attractionService.count(attractionWrapper));

        // 获取本月新增用户数
        try {
            Result<?> monthlyUserCountResult = usersClient.getMonthlyUserCount();
            if (monthlyUserCountResult != null && monthlyUserCountResult.getSuccess() && monthlyUserCountResult.getData() != null) {
                Map<String, Object> monthlyUserData = (Map<String, Object>) monthlyUserCountResult.getData();
                vo.setMonthlyNewUsers(((Number) monthlyUserData.get("monthlyUserCount")).longValue());
            } else {
                log.warn("获取本月新增用户数失败，返回结果: {}", monthlyUserCountResult);
                vo.setMonthlyNewUsers(0L);
            }
        } catch (Exception e) {
            log.error("获取本月新增用户数失败", e);
            vo.setMonthlyNewUsers(0L);
        }

        // 获取上月新增用户数
        try {
            Result<?> lastMonthlyUserCountResult = usersClient.getLastMonthlyUserCount();
            if (lastMonthlyUserCountResult != null && lastMonthlyUserCountResult.getSuccess() && lastMonthlyUserCountResult.getData() != null) {
                Map<String, Object> lastMonthlyUserData = (Map<String, Object>) lastMonthlyUserCountResult.getData();
                vo.setLastMonthlyNewUsers(((Number) lastMonthlyUserData.get("lastMonthlyUserCount")).longValue());
            } else {
                log.warn("获取上月新增用户数失败，返回结果: {}", lastMonthlyUserCountResult);
                vo.setLastMonthlyNewUsers(0L);
            }
        } catch (Exception e) {
            log.error("获取上月新增用户数失败", e);
            vo.setLastMonthlyNewUsers(0L);
        }

        // 获取本月新增游记数
        try {
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = now.withDayOfMonth(1);
            LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());

            QueryWrapper<TravelNote> monthlyTravelNoteWrapper = new QueryWrapper<>();
            monthlyTravelNoteWrapper.eq("deleted", 0);
            monthlyTravelNoteWrapper.ge("created_at", firstDayOfMonth.atStartOfDay());
            monthlyTravelNoteWrapper.le("created_at", lastDayOfMonth.atTime(23, 59, 59));
            vo.setMonthlyNewTravelNotes(travelNoteService.count(monthlyTravelNoteWrapper));
        } catch (Exception e) {
            log.error("获取本月新增游记数失败", e);
            vo.setMonthlyNewTravelNotes(0L);
        }

        // 获取上月新增游记数
        try {
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfLastMonth = now.minusMonths(1).withDayOfMonth(1);
            LocalDate lastDayOfLastMonth = now.withDayOfMonth(1).minusDays(1);

            QueryWrapper<TravelNote> lastMonthlyTravelNoteWrapper = new QueryWrapper<>();
            lastMonthlyTravelNoteWrapper.eq("deleted", 0);
            lastMonthlyTravelNoteWrapper.ge("created_at", firstDayOfLastMonth.atStartOfDay());
            lastMonthlyTravelNoteWrapper.le("created_at", lastDayOfLastMonth.atTime(23, 59, 59));
            vo.setLastMonthlyNewTravelNotes(travelNoteService.count(lastMonthlyTravelNoteWrapper));
        } catch (Exception e) {
            log.error("获取上月新增游记数失败", e);
            vo.setLastMonthlyNewTravelNotes(0L);
        }

        // 获取本月新增评论数
        try {
            Result<?> monthlyCommentCountResult = socialClient.getMonthlyCommentCount();
            if (monthlyCommentCountResult != null && monthlyCommentCountResult.getSuccess() && monthlyCommentCountResult.getData() != null) {
                Map<String, Object> monthlyCommentData = (Map<String, Object>) monthlyCommentCountResult.getData();
                vo.setMonthlyNewComments(((Number) monthlyCommentData.get("monthlyCommentCount")).longValue());
            } else {
                log.warn("获取本月新增评论数失败，返回结果: {}", monthlyCommentCountResult);
                vo.setMonthlyNewComments(0L);
            }
        } catch (Exception e) {
            log.error("获取本月新增评论数失败", e);
            vo.setMonthlyNewComments(0L);
        }

        // 获取上月新增评论数
        try {
            Result<?> lastMonthlyCommentCountResult = socialClient.getLastMonthlyCommentCount();
            if (lastMonthlyCommentCountResult != null && lastMonthlyCommentCountResult.getSuccess() && lastMonthlyCommentCountResult.getData() != null) {
                Map<String, Object> lastMonthlyCommentData = (Map<String, Object>) lastMonthlyCommentCountResult.getData();
                vo.setLastMonthlyNewComments(((Number) lastMonthlyCommentData.get("lastMonthlyCommentCount")).longValue());
            } else {
                log.warn("获取上月新增评论数失败，返回结果: {}", lastMonthlyCommentCountResult);
                vo.setLastMonthlyNewComments(0L);
            }
        } catch (Exception e) {
            log.error("获取上月新增评论数失败", e);
            vo.setLastMonthlyNewComments(0L);
        }

        // 计算增长率
        vo.setUserGrowthRate(calculateGrowthRate(vo.getMonthlyNewUsers(), vo.getLastMonthlyNewUsers()));
        vo.setTravelNoteGrowthRate(calculateGrowthRate(vo.getMonthlyNewTravelNotes(), vo.getLastMonthlyNewTravelNotes()));
        vo.setCommentGrowthRate(calculateGrowthRate(vo.getMonthlyNewComments(), vo.getLastMonthlyNewComments()));
        vo.setDestinationGrowthRate(0.0); // 目的地和景点暂不计算增长率
        vo.setAttractionGrowthRate(0.0);

        // 添加调试日志
        log.info("统计数据 - 本月新增用户: {}, 上月新增用户: {}, 用户增长率: {}%",
            vo.getMonthlyNewUsers(), vo.getLastMonthlyNewUsers(), vo.getUserGrowthRate());
        log.info("统计数据 - 本月新增游记: {}, 上月新增游记: {}, 游记增长率: {}%",
            vo.getMonthlyNewTravelNotes(), vo.getLastMonthlyNewTravelNotes(), vo.getTravelNoteGrowthRate());
        log.info("统计数据 - 本月新增评论: {}, 上月新增评论: {}, 评论增长率: {}%",
            vo.getMonthlyNewComments(), vo.getLastMonthlyNewComments(), vo.getCommentGrowthRate());

        return Result.success(vo);
    }

    /**
     * 计算增长率
     * @param monthlyNew 本月新增
     * @param lastMonthlyNew 上月新增
     * @return 增长率（百分比）
     */
    private Double calculateGrowthRate(Long monthlyNew, Long lastMonthlyNew) {
        if (lastMonthlyNew == null || lastMonthlyNew == 0) {
            // 上月没有数据，无法计算增长率
            return 0.0;
        }
        if (monthlyNew == null) {
            monthlyNew = 0L;
        }
        // 增长率 = ((本月新增 - 上月新增) / 上月新增) * 100
        return ((monthlyNew.doubleValue() - lastMonthlyNew.doubleValue()) / lastMonthlyNew.doubleValue()) * 100;
    }

    /**
     * 获取历史统计趋势（最近6个月）
     */
    @Override
    public Result<List<StatisticsTrendVO>> getStatisticsTrend() {
        LocalDate now = LocalDate.now();
        LocalDate sixMonthsAgo = now.minusMonths(5);
        return getStatisticsTrendByRange(
            sixMonthsAgo.getYear(),
            sixMonthsAgo.getMonthValue(),
            now.getYear(),
            now.getMonthValue()
        );
    }

    /**
     * 获取指定时间范围的统计趋势
     */
    @Override
    public Result<List<StatisticsTrendVO>> getStatisticsTrendByRange(int startYear, int startMonth, int endYear, int endMonth) {
        List<StatisticsTrendVO> trendList = new ArrayList<>();

        LocalDate startDate = LocalDate.of(startYear, startMonth, 1);
        LocalDate endDate = LocalDate.of(endYear, endMonth, 1);

        // 遍历从开始月份到结束月份
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int year = currentDate.getYear();
            int month = currentDate.getMonthValue();

            StatisticsTrendVO trend = new StatisticsTrendVO();
            trend.setMonth(String.format("%d-%02d", year, month));
            trend.setYear(year);
            trend.setMonthValue(month);

            // 获取用户增长率
            try {
                Result<?> currentMonthUserResult = usersClient.getMonthlyUserCountByMonth(year, month);
                Long currentMonthUsers = 0L;
                if (currentMonthUserResult != null && currentMonthUserResult.getSuccess() && currentMonthUserResult.getData() != null) {
                    Map<String, Object> userData = (Map<String, Object>) currentMonthUserResult.getData();
                    currentMonthUsers = ((Number) userData.get("monthlyUserCount")).longValue();
                }

                // 获取上个月的用户数
                LocalDate prevMonthDate = currentDate.minusMonths(1);
                Result<?> prevMonthUserResult = usersClient.getMonthlyUserCountByMonth(prevMonthDate.getYear(), prevMonthDate.getMonthValue());
                Long prevMonthUsers = 0L;
                if (prevMonthUserResult != null && prevMonthUserResult.getSuccess() && prevMonthUserResult.getData() != null) {
                    Map<String, Object> userData = (Map<String, Object>) prevMonthUserResult.getData();
                    prevMonthUsers = ((Number) userData.get("monthlyUserCount")).longValue();
                }

                trend.setUserGrowthRate(calculateGrowthRate(currentMonthUsers, prevMonthUsers));
            } catch (Exception e) {
                log.error("获取用户增长率失败: year={}, month={}", year, month, e);
                trend.setUserGrowthRate(0.0);
            }

            // 获取游记增长率
            try {
                LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
                LocalDate lastDayOfMonth = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

                QueryWrapper<TravelNote> currentMonthWrapper = new QueryWrapper<>();
                currentMonthWrapper.eq("deleted", 0);
                currentMonthWrapper.ge("created_at", firstDayOfMonth.atStartOfDay());
                currentMonthWrapper.le("created_at", lastDayOfMonth.atTime(23, 59, 59));
                Long currentMonthTravelNotes = travelNoteService.count(currentMonthWrapper);

                LocalDate prevMonthDate = currentDate.minusMonths(1);
                LocalDate firstDayOfPrevMonth = prevMonthDate.withDayOfMonth(1);
                LocalDate lastDayOfPrevMonth = prevMonthDate.withDayOfMonth(prevMonthDate.lengthOfMonth());

                QueryWrapper<TravelNote> prevMonthWrapper = new QueryWrapper<>();
                prevMonthWrapper.eq("deleted", 0);
                prevMonthWrapper.ge("created_at", firstDayOfPrevMonth.atStartOfDay());
                prevMonthWrapper.le("created_at", lastDayOfPrevMonth.atTime(23, 59, 59));
                Long prevMonthTravelNotes = travelNoteService.count(prevMonthWrapper);

                trend.setTravelNoteGrowthRate(calculateGrowthRate(currentMonthTravelNotes, prevMonthTravelNotes));
            } catch (Exception e) {
                log.error("获取游记增长率失败: year={}, month={}", year, month, e);
                trend.setTravelNoteGrowthRate(0.0);
            }

            // 获取评论增长率
            try {
                Result<?> currentMonthCommentResult = socialClient.getMonthlyCommentCountByMonth(year, month);
                Long currentMonthComments = 0L;
                if (currentMonthCommentResult != null && currentMonthCommentResult.getSuccess() && currentMonthCommentResult.getData() != null) {
                    Map<String, Object> commentData = (Map<String, Object>) currentMonthCommentResult.getData();
                    currentMonthComments = ((Number) commentData.get("monthlyCommentCount")).longValue();
                }

                LocalDate prevMonthDate = currentDate.minusMonths(1);
                Result<?> prevMonthCommentResult = socialClient.getMonthlyCommentCountByMonth(prevMonthDate.getYear(), prevMonthDate.getMonthValue());
                Long prevMonthComments = 0L;
                if (prevMonthCommentResult != null && prevMonthCommentResult.getSuccess() && prevMonthCommentResult.getData() != null) {
                    Map<String, Object> commentData = (Map<String, Object>) prevMonthCommentResult.getData();
                    prevMonthComments = ((Number) commentData.get("monthlyCommentCount")).longValue();
                }

                trend.setCommentGrowthRate(calculateGrowthRate(currentMonthComments, prevMonthComments));
            } catch (Exception e) {
                log.error("获取评论增长率失败: year={}, month={}", year, month, e);
                trend.setCommentGrowthRate(0.0);
            }

            // 目的地和景点暂不计算增长率
            trend.setDestinationGrowthRate(0.0);
            trend.setAttractionGrowthRate(0.0);

            trendList.add(trend);

            // 移动到下一个月
            currentDate = currentDate.plusMonths(1);
        }

        return Result.success(trendList);
    }
}
