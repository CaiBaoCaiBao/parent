package content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import content.pojo.dto.openhoursexception.CreateOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.DeleteOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.QueryOpenHoursExceptionDTO;
import content.pojo.dto.openhoursexception.UpdateOpenHoursExceptionDTO;
import content.pojo.entity.OpenHoursException;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("例外日期管理")
public interface OpenHoursExceptionService extends IService<OpenHoursException> {
    @Description("创建例外日期（管理员）")
    Result<?> createOpenHoursException(@Valid CreateOpenHoursExceptionDTO dto);

    @Description("批量删除例外日期（管理员）")
    Result<?> deleteOpenHoursException(@Valid DeleteOpenHoursExceptionDTO dto);

    @Description("查询例外日期列表")
    Result<?> queryOpenHoursExceptionList(QueryOpenHoursExceptionDTO dto);

    @Description("更新例外日期（管理员）")
    Result<?> updateOpenHoursException(@Valid UpdateOpenHoursExceptionDTO dto);
}
