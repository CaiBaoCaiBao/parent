package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.playitem.CreatePlayItemDTO;
import content.pojo.dto.playitem.DeletePlayItemDTO;
import content.pojo.dto.playitem.QueryPlayItemDTO;
import content.pojo.dto.playitem.UpdatePlayItemDTO;
import content.pojo.entity.PlayItem;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("游玩项目管理")
public interface PlayItemService extends IService<PlayItem> {
    @Description("创建游玩项目（管理员）")
    Result<?> createPlayItem(@Valid CreatePlayItemDTO dto);

    @Description("批量删除游玩项目（管理员）")
    Result<?> deletePlayItem(@Valid DeletePlayItemDTO dto);

    @Description("查询游玩项目列表")
    Result<?> queryPlayItemList(QueryPlayItemDTO dto);

    @Description("更新游玩项目（管理员）")
    Result<?> updatePlayItem(@Valid UpdatePlayItemDTO dto);
}
