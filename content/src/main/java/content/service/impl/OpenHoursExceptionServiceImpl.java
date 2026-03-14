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
import content.mapper.OpenHoursExceptionMapper;
import content.pojo.dto.openhoursexception.CreateOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.DeleteOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.QueryOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.UpdateOpenHoursExceptionDTO;
import content.pojo.entity.OpenHoursException;
import content.service.OpenHoursExceptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
public class OpenHoursExceptionServiceImpl extends ServiceImpl<OpenHoursExceptionMapper, OpenHoursException> implements OpenHoursExceptionService {
    @Autowired
    OpenHoursExceptionMapper openHoursExceptionMapper;
    @Autowired
    ObjectMapper objectMapper;

    @Override
    public Result<?> createOpenHoursException(CreateOpenHoursExceptionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建例外日期操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建例外日期操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        OpenHoursException openHoursException = new OpenHoursException();
        BeanUtils.copyProperties(dto, openHoursException);
        // 生成例外日期ID（使用 "OHE_" + ULID 格式，使ID更有语义）
        openHoursException.setOheId("OHE_" + common.utils.ULIDUtils.generateULID());
        if (dto.getTimeSlot() != null) {
            try {
                String timeSlotJson = objectMapper.writeValueAsString(dto.getTimeSlot());
                openHoursException.setTimeSlot(objectMapper.readValue(timeSlotJson, content.pojo.entity.TimeSlot.class));
            } catch (JsonProcessingException e) {
                log.error("时间段序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "时间段格式错误");
            }
        }
        boolean success = save(openHoursException);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deleteOpenHoursException(DeleteOpenHoursExceptionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除例外日期操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除例外日期操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<OpenHoursException> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("ohe_id", dto.getOheIds());
        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryOpenHoursExceptionList(QueryOpenHoursExceptionDTO dto) {
        QueryWrapper<OpenHoursException> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getOheId())) {
            queryWrapper.eq("ohe_id", dto.getOheId());
        }
        if (StringUtils.hasText(dto.getAttractionId())) {
            queryWrapper.eq("attraction_id", dto.getAttractionId());
        }
        if (dto.getExceptionDate() != null) {
            queryWrapper.eq("exception_date", dto.getExceptionDate());
        }
        if (dto.getExceptionType() != null) {
            queryWrapper.eq("exception_type", dto.getExceptionType());
        }
        queryWrapper.orderByAsc("exception_date");
        Page<OpenHoursException> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<OpenHoursException> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }

    @Override
    public Result<?> updateOpenHoursException(UpdateOpenHoursExceptionDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("更新例外日期操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("更新例外日期操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<OpenHoursException> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ohe_id", dto.getOheId());
        OpenHoursException openHoursException = getOne(queryWrapper);
        if (openHoursException == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "例外日期不存在");
        }
        BeanUtils.copyProperties(dto, openHoursException);
        if (dto.getTimeSlot() != null) {
            try {
                String timeSlotJson = objectMapper.writeValueAsString(dto.getTimeSlot());
                openHoursException.setTimeSlot(objectMapper.readValue(timeSlotJson, content.pojo.entity.TimeSlot.class));
            } catch (JsonProcessingException e) {
                log.error("时间段序列化失败", e);
                return Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "时间段格式错误");
            }
        }
        boolean success = updateById(openHoursException);
        return success ? Result.success("更新成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新失败");
    }
}
