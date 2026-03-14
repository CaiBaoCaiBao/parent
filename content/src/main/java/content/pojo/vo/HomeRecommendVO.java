package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.util.List;

/**
 * 首页推荐VO
 */
@Data
@Description("首页推荐内容")
public class HomeRecommendVO {

    /**
     * 推荐游记列表
     */
    private List<TravelNoteItem> recommendedTravelNotes;

    /**
     * 热门目的地列表
     */
    private List<DestinationItem> hotDestinations;

    /**
     * 热门景点列表
     */
    private List<AttractionItem> hotAttractions;

    /**
     * 游记项
     */
    @Data
    public static class TravelNoteItem {
        private String noteId;
        private String title;
        private String coverImg;
        private String summary;
        private String userId;
        private String userName;
        private String userAvatar;
        private Integer viewCount;
        private Integer likeCount;
        private Integer commentCount;
        private String createdAt;
        private Boolean top; // 是否置顶
    }

    /**
     * 目的地项
     */
    @Data
    public static class DestinationItem {
        private String destinationId;
        private String name;
        private String coverImg;
        private String description;
        private Integer viewCount;
        private String province;
        private String city;
        private String bestSeason;
    }

    /**
     * 景点项
     */
    @Data
    public static class AttractionItem {
        private String aid;
        private String destinationId;
        private String destinationName;
        private String name;
        private String coverImg;
        private String description;
        private Integer viewCount;
    }
}
