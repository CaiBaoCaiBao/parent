package content.controller;

import common.utils.Result;
import content.pojo.dto.ticket.CreateTicketDTO;
import content.pojo.dto.ticket.DeleteTicketDTO;
import content.pojo.dto.ticket.QueryTicketDTO;
import content.pojo.dto.ticket.UpdateTicketDTO;
import content.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ticket")
public class TicketController {
    @Autowired
    TicketService ticketService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建门票")
    public Result<?> createTicket(@RequestBody @Valid CreateTicketDTO dto) {
        return ticketService.createTicket(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete")
    @Description(value = "批量删除门票")
    public Result<?> deleteTicket(@RequestBody @Valid DeleteTicketDTO dto) {
        return ticketService.deleteTicket(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询门票列表")
    public Result<?> queryTicketList(@ModelAttribute QueryTicketDTO dto) {
        return ticketService.queryTicketList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/update")
    @Description(value = "更新门票")
    public Result<?> updateTicket(@RequestBody @Valid UpdateTicketDTO dto) {
        return ticketService.updateTicket(dto);
    }
}
