package content.pojo.dto.destination;

import lombok.Data;
import org.springframework.context.annotation.Description;

@Data
@Description("目的地查询DTO")
public class QueryDestinationDTO {

    private String name;           // 目的地名称（模糊搜索）
    private String province;      // 省份
    private String city;          // 城市
    private Integer level;         // 层级：1-城市，2-景区
    private Integer status;         // 状态：0-禁用，1-启用
    private Integer pageNum = 1;  // 页码
    private Integer pageSize = 10; // 每页条数
}
