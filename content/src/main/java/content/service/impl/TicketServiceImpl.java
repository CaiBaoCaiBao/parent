package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import content.mapper.TicketMapper;
import content.pojo.dto.ticket.CreateTicketDTO;
import content.pojo.dto.ticket.DeleteTicketDTO;
import content.pojo.dto.ticket.QueryTicketDTO;
import content.pojo.dto.ticket.UpdateTicketDTO;
import content.pojo.entity.Ticket;
import content.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {
    @Autowired
    TicketMapper ticketMapper;

    @Override
    public Result<?> createTicket(CreateTicketDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建门票操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建门票操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        Ticket ticket = new Ticket();
        BeanUtils.copyProperties(dto, ticket);
        // 生成票种ID（使用 "TICKET_" + ULID 格式，使ID更有语义）
        ticket.setTid("TICKET_" + common.utils.ULIDUtils.generateULID());
        // 设置默认状态为启用（1）
        if (ticket.getStatus() == null) {
            ticket.setStatus(1);
        }
        boolean success = save(ticket);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deleteTicket(DeleteTicketDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除门票操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除门票操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<Ticket> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("tid", dto.getTids());
        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryTicketList(QueryTicketDTO dto) {
        QueryWrapper<Ticket> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getTid())) {
            queryWrapper.eq("tid", dto.getTid());
        }
        if (StringUtils.hasText(dto.getAttractionId())) {
            queryWrapper.eq("attraction_id", dto.getAttractionId());
        }
        if (StringUtils.hasText(dto.getPlayItemId())) {
            queryWrapper.eq("play_item_id", dto.getPlayItemId());
        }
        if (StringUtils.hasText(dto.getTicketType())) {
            queryWrapper.eq("ticket_type", dto.getTicketType());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        queryWrapper.orderByAsc("sort_order");
        Page<Ticket> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<Ticket> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }

    @Override
    public Result<?> updateTicket(UpdateTicketDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("更新门票操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("更新门票操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<Ticket> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("tid", dto.getTid());
        Ticket ticket = getOne(queryWrapper);
        if (ticket == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "门票不存在");
        }
        BeanUtils.copyProperties(dto, ticket);
        boolean success = updateById(ticket);
        return success ? Result.success("更新成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新失败");
    }
}
