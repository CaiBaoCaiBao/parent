package content.controller;

import common.utils.Result;
import content.pojo.dto.banner.CreateBannerDTO;
import content.pojo.dto.banner.DeleteBannerDTO;
import content.pojo.dto.banner.QueryBannerDTO;
import content.pojo.dto.banner.UpdateBannerDTO;
import content.service.BannerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图管理控制器
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 创建轮播图
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/create")
    @Description(value = "创建轮播图")
    public Result<?> createBanner(@RequestBody @Valid CreateBannerDTO dto) {
        return bannerService.createBanner(dto);
    }

    /**
     * 批量删除轮播图
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/admin-api/delete")
    @Description(value = "批量删除轮播图")
    public Result<?> deleteBanner(@RequestBody @Valid DeleteBannerDTO dto) {
        return bannerService.deleteBanner(dto);
    }

    /**
     * 查询轮播图列表
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/admin-api/list")
    @Description(value = "查询轮播图列表")
    public Result<?> queryBannerList(@ModelAttribute QueryBannerDTO dto) {
        return bannerService.queryBannerList(dto);
    }

    /**
     * 更新轮播图
     * 仅超级管理员可访问
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/admin-api/update")
    @Description(value = "更新轮播图")
    public Result<?> updateBanner(@RequestBody @Valid UpdateBannerDTO dto) {
        return bannerService.updateBanner(dto);
    }

    /**
     * 获取启用的轮播图列表
     * 游客和普通用户都可以访问
     */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/active")
    @Description(value = "获取启用的轮播图列表")
    public Result<?> getActiveBannerList() {
        return bannerService.getActiveBannerList();
    }
}
