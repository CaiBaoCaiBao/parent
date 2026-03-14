package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import content.mapper.DestinationMapper;
import content.mapper.DestinationTagsMapper;
import content.pojo.dto.destinationtags.CreateDestinationTagsDTO;
import content.pojo.dto.destinationtags.DeleteDestinationTagsDTO;
import content.pojo.dto.destinationtags.QueryDestinationTagsDTO;
import content.pojo.entity.Destination;
import content.pojo.entity.DestinationTags;
import content.service.DestinationTagsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
public class DestinationTagsServiceImpl extends ServiceImpl<DestinationTagsMapper, DestinationTags> implements DestinationTagsService {
    @Autowired
    DestinationTagsMapper destinationTagsMapper;
    @Autowired
    DestinationMapper destinationMapper;

    @Override
    public Result<?> createDestinationTags(CreateDestinationTagsDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建目的地标签关联操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建目的地标签关联操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
        destinationQuery.eq("destination_id", dto.getDestinationId());
        Destination destination = destinationMapper.selectOne(destinationQuery);
        if (destination == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "目的地不存在");
        }
        DestinationTags destinationTags = new DestinationTags();
        BeanUtils.copyProperties(dto, destinationTags);
        destinationTags.setDestinationId(destination.getId());
        boolean success = save(destinationTags);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deleteDestinationTags(DeleteDestinationTagsDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除目的地标签关联操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除目的地标签关联操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<DestinationTags> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", dto.getIds());
        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryDestinationTagsList(QueryDestinationTagsDTO dto) {
        QueryWrapper<DestinationTags> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getDestinationId())) {
            QueryWrapper<Destination> destinationQuery = new QueryWrapper<>();
            destinationQuery.eq("destination_id", dto.getDestinationId());
            Destination destination = destinationMapper.selectOne(destinationQuery);
            if (destination != null) {
                queryWrapper.eq("destination_id", destination.getId());
            }
        }
        if (dto.getTagId() != null) {
            queryWrapper.eq("tag_id", dto.getTagId());
        }
        if (dto.getRecommendFlag() != null) {
            queryWrapper.eq("recommend_flag", dto.getRecommendFlag());
        }
        queryWrapper.orderByDesc("weight");
        Page<DestinationTags> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<DestinationTags> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }
}
