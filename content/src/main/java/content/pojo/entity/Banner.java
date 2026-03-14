package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

/**
 * 轮播图
 */
@Data
@TableName("t_banner")
@Description("轮播图")
public class Banner {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String image;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 链接类型：0-无，1-游记，2-目的地
     */
    private Integer linkType;

    /**
     * 目标ID（游记ID或目的地ID）
     */
    private String targetId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
