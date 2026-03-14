package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.banner.CreateBannerDTO;
import content.pojo.dto.banner.DeleteBannerDTO;
import content.pojo.dto.banner.QueryBannerDTO;
import content.pojo.dto.banner.UpdateBannerDTO;
import content.pojo.entity.Banner;
import content.pojo.vo.BannerVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

/**
 * 轮播图管理服务
 */
@Description("轮播图管理")
public interface BannerService extends IService<Banner> {

    /**
     * 创建轮播图
     */
    @Description("创建轮播图")
    Result<?> createBanner(@Valid CreateBannerDTO dto);

    /**
     * 批量删除轮播图
     */
    @Description("批量删除轮播图")
    Result<?> deleteBanner(@Valid DeleteBannerDTO dto);

    /**
     * 查询轮播图列表
     */
    @Description("查询轮播图列表")
    Result<?> queryBannerList(QueryBannerDTO dto);

    /**
     * 更新轮播图
     */
    @Description("更新轮播图")
    Result<?> updateBanner(@Valid UpdateBannerDTO dto);

    /**
     * 获取启用的轮播图列表（用于首页展示）
     */
    @Description("获取启用的轮播图列表")
    Result<?> getActiveBannerList();
}
