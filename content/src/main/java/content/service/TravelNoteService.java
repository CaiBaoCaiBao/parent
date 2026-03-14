package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.travelnote.AuditTravelNoteDTO;
import content.pojo.dto.travelnote.CreateTravelNoteDTO;
import content.pojo.dto.travelnote.DeleteTravelNoteDTO;
import content.pojo.dto.travelnote.GetTravelNoteDetailDTO;
import content.pojo.dto.travelnote.QueryTravelNoteDTO;
import content.pojo.dto.travelnote.SetTopTravelNoteDTO;
import content.pojo.dto.travelnote.UpdateTravelNoteDTO;
import content.pojo.entity.TravelNote;
import content.pojo.vo.TravelNoteDetailVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

import java.util.List;

@Description("游记管理")
public interface TravelNoteService extends IService<TravelNote> {
    @Description("创建游记")
    Result<?> createTravelNote(@Valid CreateTravelNoteDTO dto);

    @Description("批量删除游记")
    Result<?> deleteTravelNote(@Valid DeleteTravelNoteDTO dto);

    @Description("查询游记列表")
    Result<?> queryTravelNoteList(QueryTravelNoteDTO dto);

    @Description("更新游记")
    Result<?> updateTravelNote(@Valid UpdateTravelNoteDTO dto);

    @Description("获取游记详情")
    Result<TravelNoteDetailVO> getTravelNoteDetail(@Valid GetTravelNoteDetailDTO dto);

    @Description("审核游记")
    Result<?> auditTravelNote(@Valid AuditTravelNoteDTO dto);

    @Description("置顶/取消置顶游记")
    Result<?> setTopTravelNote(@Valid SetTopTravelNoteDTO dto);

    @Description("批量获取游记详情")
    Result<?> getBatchTravelNoteDetail(List<String> noteIds);
}
