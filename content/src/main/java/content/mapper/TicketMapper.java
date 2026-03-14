package content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import content.pojo.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TicketMapper extends BaseMapper<Ticket> {
}
