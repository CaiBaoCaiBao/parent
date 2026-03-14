package content.pojo.vo.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchResultVO {

    /**
     * 游记列表
     */
    private List<TravelNoteItem> travelNotes;

    /**
     * 目的地列表
     */
    private List<DestinationItem> destinations;

    /**
     * 游记总数
     */
    private Long travelNoteTotal;

    /**
     * 目的地总数
     */
    private Long destinationTotal;

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
    }

    @Data
    public static class DestinationItem {
        private String destinationId;
        private String name;
        private String coverImg;
        private String description;
        private String province;
        private String city;
        private Integer attractionCount;
        private Integer travelNoteCount;
        private String createdAt;
    }
}
