package content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import content.pojo.dto.travelnote.QueryTravelNoteDTO;
import content.pojo.entity.TravelNote;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TravelNoteMapper extends BaseMapper<TravelNote> {

    /**
     * 查询游记列表（包含用户和目的地详细信息）
     */
    List<TravelNote> queryTravelNoteListWithDetails(@Param("dto") QueryTravelNoteDTO dto);

    /**
     * 统计游记数量
     */
    Long countTravelNoteListWithDetails(@Param("dto") QueryTravelNoteDTO dto);
}
