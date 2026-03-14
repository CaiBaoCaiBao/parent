package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.ticket.CreateTicketDTO;
import content.pojo.dto.ticket.DeleteTicketDTO;
import content.pojo.dto.ticket.QueryTicketDTO;
import content.pojo.dto.ticket.UpdateTicketDTO;
import content.pojo.entity.Ticket;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("门票管理")
public interface TicketService extends IService<Ticket> {
    @Description("创建门票（管理员）")
    Result<?> createTicket(@Valid CreateTicketDTO dto);

    @Description("批量删除门票（管理员）")
    Result<?> deleteTicket(@Valid DeleteTicketDTO dto);

    @Description("查询门票列表")
    Result<?> queryTicketList(QueryTicketDTO dto);

    @Description("更新门票（管理员）")
    Result<?> updateTicket(@Valid UpdateTicketDTO dto);
}
