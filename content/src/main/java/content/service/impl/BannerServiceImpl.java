package content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import common.context.UserContext;
import common.dict.DictConstants;
import common.enums.ResultCode;
import common.utils.PageResult;
import common.utils.Result;
import content.mapper.BannerMapper;
import content.pojo.dto.banner.CreateBannerDTO;
import content.pojo.dto.banner.DeleteBannerDTO;
import content.pojo.dto.banner.QueryBannerDTO;
import content.pojo.dto.banner.UpdateBannerDTO;
import content.pojo.entity.Banner;
import content.pojo.vo.BannerVO;
import content.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 轮播图管理服务实现
 */
@Slf4j
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public Result<?> createBanner(CreateBannerDTO dto) {
        // 获取当前用户信息
        String currentRole = UserContext.getRole();
        String currentStatus = UserContext.getStatus();

        // 只有管理员才能创建轮播图
        if (!DictConstants.UserRole.ADMIN.equals(currentRole)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "只有管理员才能创建轮播图");
        }

        // 检查当前用户状态
        if (DictConstants.UserStatus.INACTIVE.equals(currentStatus)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }

        // 验证链接类型和目标ID
        if (dto.getLinkType() != null && dto.getLinkType() != 0) {
            if (!StringUtils.hasText(dto.getTargetId())) {
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "链接类型为游记或目的地时，目标ID不能为空");
            }
        }

        // 创建轮播图
        Banner banner = new Banner();
        BeanUtils.copyProperties(dto, banner);
        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());

        boolean result = save(banner);
        if (!result) {
            log.warn("创建轮播图失败");
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "创建轮播图失败");
        }

        log.info("管理员创建轮播图成功，ID: {}", banner.getId());
        return Result.success("创建成功");
    }

    @Override
    public Result<?> deleteBanner(DeleteBannerDTO dto) {
        // 获取当前用户信息
        String currentRole = UserContext.getRole();
        String currentStatus = UserContext.getStatus();

        // 只有管理员才能删除轮播图
        if (!DictConstants.UserRole.ADMIN.equals(currentRole)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "只有管理员才能删除轮播图");
        }

        // 检查当前用户状态
        if (DictConstants.UserStatus.INACTIVE.equals(currentStatus)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }

        // 批量删除
        boolean result = removeByIds(dto.getIds());
        if (!result) {
            log.warn("删除轮播图失败");
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "删除轮播图失败");
        }

        log.info("管理员删除轮播图成功，IDs: {}", dto.getIds());
        return Result.success("删除成功");
    }

    @Override
    public Result<?> queryBannerList(QueryBannerDTO dto) {
        // 构建查询条件
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();

        // 标题模糊查询
        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.like("title", dto.getTitle());
        }

        // 链接类型
        if (dto.getLinkType() != null) {
            wrapper.eq("link_type", dto.getLinkType());
        }

        // 状态
        if (dto.getStatus() != null) {
            wrapper.eq("status", dto.getStatus());
        }

        // 按排序和创建时间排序
        wrapper.orderByAsc("sort").orderByDesc("created_at");

        // 分页查询
        Page<Banner> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Banner> bannerPage = page(page, wrapper);

        // 转换为VO
        List<BannerVO> voList = bannerPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<BannerVO> pageResult = new PageResult<>(
                voList,
                bannerPage.getTotal(),
                (long) dto.getPage(),
                (long) dto.getPageSize()
        );

        return Result.success(pageResult);
    }

    @Override
    public Result<?> updateBanner(UpdateBannerDTO dto) {
        // 获取当前用户信息
        String currentRole = UserContext.getRole();
        String currentStatus = UserContext.getStatus();

        // 只有管理员才能更新轮播图
        if (!DictConstants.UserRole.ADMIN.equals(currentRole)) {
            return Result.error(ResultCode.VALIDATE_FAILED.getCode(), "只有管理员才能更新轮播图");
        }

        // 检查当前用户状态
        if (DictConstants.UserStatus.INACTIVE.equals(currentStatus)) {
            return Result.error(ResultCode.ACCOUNT_DISABLED.getCode(), "账号已被禁用，无法执行此操作");
        }

        // 查询轮播图是否存在
        Banner existingBanner = getById(dto.getId());
        if (existingBanner == null) {
            return Result.error(ResultCode.DATA_NOT_FOUND.getCode(), "轮播图不存在");
        }

        // 验证链接类型和目标ID
        if (dto.getLinkType() != null && dto.getLinkType() != 0) {
            if (!StringUtils.hasText(dto.getTargetId())) {
                return Result.error(ResultCode.PARAM_ERROR.getCode(), "链接类型为游记或目的地时，目标ID不能为空");
            }
        }

        // 更新轮播图
        Banner banner = new Banner();
        BeanUtils.copyProperties(dto, banner);
        banner.setUpdatedAt(LocalDateTime.now());

        boolean result = updateById(banner);
        if (!result) {
            log.warn("更新轮播图失败，ID: {}", dto.getId());
            return Result.error(ResultCode.DATABASE_OPERATION_FAILED.getCode(), "更新轮播图失败");
        }

        log.info("管理员更新轮播图成功，ID: {}", dto.getId());
        return Result.success("更新成功");
    }

    @Override
    public Result<?> getActiveBannerList() {
        // 查询启用的轮播图
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 启用状态
        wrapper.orderByAsc("sort").orderByDesc("created_at");

        List<Banner> banners = list(wrapper);

        // 转换为VO
        List<BannerVO> voList = banners.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 转换为VO
     */
    private BannerVO convertToVO(Banner banner) {
        BannerVO vo = new BannerVO();
        BeanUtils.copyProperties(banner, vo);
        return vo;
    }
}
