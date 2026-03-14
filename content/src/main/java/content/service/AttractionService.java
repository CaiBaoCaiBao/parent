package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.attraction.CreateAttractionDTO;
import content.pojo.dto.attraction.DeleteAttractionDTO;
import content.pojo.dto.attraction.GetAttractionDetailDTO;
import content.pojo.dto.attraction.QueryAttractionDTO;
import content.pojo.dto.attraction.UpdateAttractionDTO;
import content.pojo.entity.Attraction;
import content.pojo.vo.AttractionDetailVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("景点管理")
public interface AttractionService extends IService<Attraction> {
    @Description("创建景点（管理员）")
    Result<?> createAttraction(@Valid CreateAttractionDTO dto);

    @Description("批量删除景点（管理员）")
    Result<?> deleteAttraction(@Valid DeleteAttractionDTO dto);

    @Description("查询景点列表")
    Result<?> queryAttractionList(QueryAttractionDTO dto);

    @Description("更新景点（管理员）")
    Result<?> updateAttraction(@Valid UpdateAttractionDTO dto);

    @Description("获取景点详情")
    Result<AttractionDetailVO> getAttractionDetail(@Valid GetAttractionDetailDTO dto);
}
