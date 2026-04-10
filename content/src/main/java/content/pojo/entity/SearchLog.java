package content.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 搜索记录实体类
 */
@Data
@TableName("search_log")
public class SearchLog {

    /**
     * 记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 用户ID（可为空，支持游客搜索）
     */
    private String userId;

    /**
     * 搜索类型：all/destination/travel_note/attraction
     */
    private String searchType;

    /**
     * 搜索结果数量
     */
    private Integer resultCount;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 搜索时间
     */
    private LocalDateTime createdAt;
}
