package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import content.mapper.OpenTimeRuleMapper;
import content.pojo.dto.opentimerule.CreateOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.DeleteOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.QueryOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.UpdateOpenTimeRuleDTO;
import content.pojo.entity.OpenTimeRule;
import content.service.OpenTimeRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenTimeRuleServiceImpl extends ServiceImpl<OpenTimeRuleMapper, OpenTimeRule> implements OpenTimeRuleService {
    @Autowired
    OpenTimeRuleMapper openTimeRuleMapper;
    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Result<?> createOpenTimeRule(CreateOpenTimeRuleDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建开放时间规则操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建开放时间规则操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        OpenTimeRule openTimeRule = new OpenTimeRule();
        BeanUtils.copyProperties(dto, openTimeRule);
        // 生成开放时间规则ID（使用 "OTR_" + ULID 格式，使ID更有语义）
        openTimeRule.setOtrId("OTR_" + common.utils.ULIDUtils.generateULID());
        if (dto.getTimeSlots() != null && !dto.getTimeSlots().isEmpty()) {
            try {
                String timeSlotsJson = objectMapper.writeValueAsString(dto.getTimeSlots());
                openTimeRule.setTimeSlots(objectMapper.readValue(timeSlotsJson,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, content.pojo.entity.TimeSlot.class)));
            } catch (JsonProcessingException e) {
                log.error("时间段序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "时间段格式错误");
            }
        }
        boolean success = save(openTimeRule);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deleteOpenTimeRule(DeleteOpenTimeRuleDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除开放时间规则操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除开放时间规则操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<OpenTimeRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("otr_id", dto.getOtrIds());
        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryOpenTimeRuleList(QueryOpenTimeRuleDTO dto) {
        QueryWrapper<OpenTimeRule> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getOtrId())) {
            queryWrapper.eq("otr_id", dto.getOtrId());
        }
        if (StringUtils.hasText(dto.getAttractionId())) {
            queryWrapper.eq("attraction_id", dto.getAttractionId());
        }
        if (StringUtils.hasText(dto.getScheduleType())) {
            queryWrapper.eq("schedule_type", dto.getScheduleType());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        queryWrapper.orderByAsc("priority");
        Page<OpenTimeRule> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<OpenTimeRule> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }

    @Override
    public Result<?> updateOpenTimeRule(UpdateOpenTimeRuleDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("更新开放时间规则操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("更新开放时间规则操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<OpenTimeRule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("otr_id", dto.getOtrId());
        OpenTimeRule openTimeRule = getOne(queryWrapper);
        if (openTimeRule == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "开放时间规则不存在");
        }
        BeanUtils.copyProperties(dto, openTimeRule);
        if (dto.getTimeSlots() != null && !dto.getTimeSlots().isEmpty()) {
            try {
                String timeSlotsJson = objectMapper.writeValueAsString(dto.getTimeSlots());
                openTimeRule.setTimeSlots(objectMapper.readValue(timeSlotsJson,
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, content.pojo.entity.TimeSlot.class)));
            } catch (JsonProcessingException e) {
                log.error("时间段序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "时间段格式错误");
            }
        }
        boolean success = updateById(openTimeRule);
        return success ? Result.success("更新成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新失败");
    }
}
