package social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import social.pojo.dto.collection.CheckCollectionStatusDTO;
import social.pojo.dto.collection.QueryCollectionListDTO;
import social.pojo.dto.collection.ToggleCollectionDTO;
import social.pojo.entity.Collection;
import social.pojo.vo.CollectionStatusVO;
import social.pojo.vo.CollectionVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("收藏管理")
public interface CollectionService extends IService<Collection> {
    @Description("收藏/取消收藏")
    Result<?> toggleCollection(@Valid ToggleCollectionDTO dto);

    @Description("检查收藏状态")
    Result<CollectionStatusVO> checkCollectionStatus(@Valid CheckCollectionStatusDTO dto);

    @Description("查询收藏列表")
    Result<?> queryCollectionList(@Valid QueryCollectionListDTO dto);
}
