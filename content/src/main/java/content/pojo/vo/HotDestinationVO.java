package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

/**
 * 热门目的地排行VO
 */
@Data
@Description("热门目的地排行VO")
public class HotDestinationVO {

    /**
     * 热门目的地列表
     */
    private List<DestinationRankingItem> destinations;

    /**
     * 目的地排行项
     */
    @Data
    public static class DestinationRankingItem {
        /**
         * 排名
         */
        private Integer ranking;

        /**
         * 目的地ID
         */
        private String destinationId;

        /**
         * 目的地名称
         */
        private String name;

        /**
         * 封面图片
         */
        private String coverImg;

        /**
         * 描述
         */
        private String description;

        /**
         * 浏览量
         */
        private Integer viewCount;

        /**
         * 省份
         */
        private String province;

        /**
         * 城市
         */
        private String city;

        /**
         * 最佳旅游季节
         */
        private String bestSeason;
    }
}
