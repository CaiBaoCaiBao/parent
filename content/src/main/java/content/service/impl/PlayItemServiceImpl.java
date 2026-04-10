package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.Result;
import content.mapper.PlayItemMapper;
import content.pojo.dto.playitem.CreatePlayItemDTO;
import content.pojo.dto.playitem.DeletePlayItemDTO;
import content.pojo.dto.playitem.QueryPlayItemDTO;
import content.pojo.dto.playitem.UpdatePlayItemDTO;
import content.pojo.entity.PlayItem;
import content.service.PlayItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Slf4j
@Service
public class PlayItemServiceImpl extends ServiceImpl<PlayItemMapper, PlayItem> implements PlayItemService {

    @Override
    public Result<?> createPlayItem(CreatePlayItemDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("创建游玩项目操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("创建游玩项目操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        PlayItem playItem = new PlayItem();
        BeanUtils.copyProperties(dto, playItem);
        // 生成游玩项目ID（使用 "PLAY_ITEM_" + ULID 格式，使ID更有语义）
        playItem.setPiid("PLAY_ITEM_" + common.utils.ULIDUtils.generateULID());
        // 设置景点ID
        playItem.setAid(dto.getAttractionId());
        // 设置默认状态为启用（1）
        if (playItem.getStatus() == null) {
            playItem.setStatus(1);
        }
        // images 字段会自动通过 JacksonTypeHandler 处理，无需手动序列化
        boolean success = save(playItem);
        return success ? Result.success("创建成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "创建失败");
    }

    @Override
    public Result<?> deletePlayItem(DeletePlayItemDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("删除游玩项目操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("删除游玩项目操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<PlayItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("piid", dto.getPiids());
        boolean success = remove(queryWrapper);
        return success ? Result.success("删除成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "删除失败");
    }

    @Override
    public Result<?> queryPlayItemList(QueryPlayItemDTO dto) {
        QueryWrapper<PlayItem> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(dto.getPiid())) {
            queryWrapper.eq("piid", dto.getPiid());
        }
        if (StringUtils.hasText(dto.getAttractionId())) {
            queryWrapper.eq("aid", dto.getAttractionId());
        }
        if (StringUtils.hasText(dto.getName())) {
            queryWrapper.like("name", dto.getName());
        }
        if (dto.getStatus() != null) {
            queryWrapper.eq("status", dto.getStatus());
        }
        queryWrapper.orderByAsc("created_at");
        Page<PlayItem> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        Page<PlayItem> resultPage = page(page, queryWrapper);
        return Result.success(resultPage);
    }

    @Override
    public Result<?> updatePlayItem(UpdatePlayItemDTO dto) {
        String currentUid = UserContext.getUserUUid();
        String role = UserContext.getRole();
        String status = UserContext.getStatus();
        if (!Objects.equals(role, DictConstants.UserRole.ADMIN)) {
            log.info("更新游玩项目操作者：{} 没有操作权限", currentUid);
            return Result.error(ResultCode.TOKEN_PARSE_ERROR.getCode(), "没有操作权限");
        }
        if (!Objects.equals(status, DictConstants.UserStatus.ACTIVE)) {
            log.info("更新游玩项目操作者：{} 账户处于封禁", currentUid);
            return Result.error(ResultCode.ACCOUNT_LOCKED.getCode(), "账户处于封禁，无法进行该操作");
        }
        QueryWrapper<PlayItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("piid", dto.getPiid());
        PlayItem playItem = getOne(queryWrapper);
        if (playItem == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "游玩项目不存在");
        }
        BeanUtils.copyProperties(dto, playItem);
        // 手动设置景点ID（因为字段名不同）
        if (dto.getAttractionId() != null) {
            playItem.setAid(dto.getAttractionId());
        }
        // images 字段会自动通过 JacksonTypeHandler 处理，无需手动序列化
        boolean success = updateById(playItem);
        return success ? Result.success("更新成功") : Result.error(ResultCode.DATA_OPERATION_FAILED.getCode(), "更新失败");
    }
}
