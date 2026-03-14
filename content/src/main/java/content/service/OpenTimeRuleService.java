package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.opentimerule.CreateOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.DeleteOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.QueryOpenTimeRuleDTO;
import content.pojo.dto.opentimerule.UpdateOpenTimeRuleDTO;
import content.pojo.entity.OpenTimeRule;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("开放时间规则管理")
public interface OpenTimeRuleService extends IService<OpenTimeRule> {
    @Description("创建开放时间规则（管理员）")
    Result<?> createOpenTimeRule(@Valid CreateOpenTimeRuleDTO dto);

    @Description("批量删除开放时间规则（管理员）")
    Result<?> deleteOpenTimeRule(@Valid DeleteOpenTimeRuleDTO dto);

    @Description("查询开放时间规则列表")
    Result<?> queryOpenTimeRuleList(QueryOpenTimeRuleDTO dto);

    @Description("更新开放时间规则（管理员）")
    Result<?> updateOpenTimeRule(@Valid UpdateOpenTimeRuleDTO dto);
}
