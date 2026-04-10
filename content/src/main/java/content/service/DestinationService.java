package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.destination.CreateDestinationDTO;
import content.pojo.dto.destination.DeleteDestinationDTO;
import content.pojo.dto.destination.GetDestinationDetailDTO;
import content.pojo.dto.destination.QueryDestinationDTO;
import content.pojo.dto.destination.UpdateDestinationDTO;
import content.pojo.entity.Destination;
import content.pojo.vo.DestinationDetailVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("目的地管理")
public interface DestinationService extends IService<Destination> {
    @Description("创建目的地（管理员）")
    Result<?> createDestination(@Valid CreateDestinationDTO dto);
    @Description("批量删除目的地（管理员）")
    Result<?> deleteDestination(@Valid DeleteDestinationDTO dto);
    @Description("查询目的地列表")
    Result<?> queryDestinationList(QueryDestinationDTO dto);
    @Description("更新目的地（管理员）")
    Result<?> updateDestination(@Valid UpdateDestinationDTO dto);
    @Description("获取目的地详情")
    Result<DestinationDetailVO> getDestinationDetail(@Valid GetDestinationDetailDTO dto);

    @Description("批量获取目的地详情")
    Result<?> getBatchDestinationDetail(java.util.List<String> destinationIds);

    @Description("增加目的地浏览数")
    Result<?> incrementViewCount(String destinationId);
}
