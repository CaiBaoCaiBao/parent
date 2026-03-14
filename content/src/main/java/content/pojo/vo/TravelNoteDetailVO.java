package content.pojo.vo;

import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Description("游记详情VO")
public class TravelNoteDetailVO {
    private Long id;
    private String noteId;
    private String userId;
    private String userName;
    private String nickName;
    private String userAvatar;
    private String destinationId;
    private String destinationName;
    private String title;
    private String coverImg;
    private List<String> images;
    private String content;
    private Integer travelDays;
    private Double budget;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联信息
    private List<AttractionSimpleVO> attractions; // 景点列表

    @Data
    public static class AttractionSimpleVO {
        private Long id;
        private String aid;
        private String name;
        private String coverImg;
        private String description;
    }
}
