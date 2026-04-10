package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

/**
 * 系统统计概况VO
 */
@Data
@Description("系统统计概况VO")
public class StatisticsOverviewVO {

    /**
     * 用户总数
     */
    private Long totalUsers;

    /**
     * 游记总数
     */
    private Long totalTravelNotes;

    /**
     * 评论总数
     */
    private Long totalComments;

    /**
     * 目的地总数
     */
    private Long totalDestinations;

    /**
     * 景点总数
     */
    private Long totalAttractions;

    /**
     * 本月新增用户数
     */
    private Long monthlyNewUsers;

    /**
     * 本月新增游记数
     */
    private Long monthlyNewTravelNotes;

    /**
     * 本月新增评论数
     */
    private Long monthlyNewComments;

    /**
     * 上月新增用户数
     */
    private Long lastMonthlyNewUsers;

    /**
     * 上月新增游记数
     */
    private Long lastMonthlyNewTravelNotes;

    /**
     * 上月新增评论数
     */
    private Long lastMonthlyNewComments;

    /**
     * 用户增长率（百分比）
     */
    private Double userGrowthRate;

    /**
     * 游记增长率（百分比）
     */
    private Double travelNoteGrowthRate;

    /**
     * 评论增长率（百分比）
     */
    private Double commentGrowthRate;

    /**
     * 目的地增长率（百分比）
     */
    private Double destinationGrowthRate;

    /**
     * 景点增长率（百分比）
     */
    private Double attractionGrowthRate;
}
