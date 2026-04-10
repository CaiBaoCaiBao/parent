package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

/**
 * 历史统计趋势VO
 */
@Data
@Description("历史统计趋势VO")
public class StatisticsTrendVO {

    /**
     * 月份标签（如 "2025-10", "2025-11", "2025-12"）
     */
    private String month;

    /**
     * 年份
     */
    private Integer year;

    /**
     * 月份
     */
    private Integer monthValue;

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
