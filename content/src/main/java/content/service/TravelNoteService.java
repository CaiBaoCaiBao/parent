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

    @Description("保存游记草稿")
    Result<?> saveDraft(@Valid CreateTravelNoteDTO dto);

    @Description("发布草稿")
    Result<?> publishDraft(@Valid UpdateTravelNoteDTO dto);

    @Description("批量删除游记")
    Result<?> deleteTravelNote(@Valid DeleteTravelNoteDTO dto);

    @Description("查询游记列表")
    Result<?> queryTravelNoteList(QueryTravelNoteDTO dto);

    @Description("查询我的游记列表（需要登录，可查看所有状态的游记）")
    Result<?> queryMyTravelNoteList(QueryTravelNoteDTO dto);

    @Description("获取热门游记列表")
    Result<?> getHotTravelNotes(Integer pageNum, Integer pageSize);

    @Description("更新游记")
    Result<?> updateTravelNote(@Valid UpdateTravelNoteDTO dto);

    @Description("获取游记详情")
    Result<TravelNoteDetailVO> getTravelNoteDetail(@Valid GetTravelNoteDetailDTO dto);

    @Description("获取我的游记详情（需要登录，可访问自己的草稿）")
    Result<TravelNoteDetailVO> getMyTravelNoteDetail(@Valid GetTravelNoteDetailDTO dto);

    @Description("审核游记")
    Result<?> auditTravelNote(@Valid AuditTravelNoteDTO dto);

    @Description("管理员查询游记列表（需要管理员权限，可查看所有状态的游记）")
    Result<?> queryTravelNoteListForAdmin(QueryTravelNoteDTO dto);

    @Description("置顶/取消置顶游记")
    Result<?> setTopTravelNote(@Valid SetTopTravelNoteDTO dto);

    @Description("批量获取游记详情")
    Result<?> getBatchTravelNoteDetail(List<String> noteIds);

    @Description("增加游记浏览数")
    Result<?> incrementViewCount(String noteId);
}
