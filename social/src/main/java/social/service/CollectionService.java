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

import java.util.List;
import java.util.Map;

@Description("收藏管理")
public interface CollectionService extends IService<Collection> {
    @Description("收藏/取消收藏")
    Result<?> toggleCollection(@Valid ToggleCollectionDTO dto);

    @Description("检查收藏状态")
    Result<CollectionStatusVO> checkCollectionStatus(@Valid CheckCollectionStatusDTO dto);

    @Description("查询收藏列表")
    Result<?> queryCollectionList(@Valid QueryCollectionListDTO dto);

    @Description("批量获取收藏数")
    Result<Map<String, Integer>> getBatchCollectionCount(List<String> targetIds);

    @Description("获取单个目标的收藏数量")
    Result<Integer> getCollectionCount(String targetId);

    @Description("根据目标类型和目标ID列表批量删除收藏")
    Result<?> deleteCollectionsByTargetIds(String targetType, List<String> targetIds);
}
