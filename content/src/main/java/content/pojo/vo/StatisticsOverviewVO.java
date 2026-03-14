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
}
