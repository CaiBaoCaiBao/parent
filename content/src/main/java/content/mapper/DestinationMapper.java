package content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import content.pojo.entity.Destination;
import jakarta.validation.constraints.NotNull;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.context.annotation.Description;

@Mapper
public interface DestinationMapper extends BaseMapper<Destination> {
    @Description("根据ID查询目的地")
    Destination selectByDestinationId(@NotNull(message = "ID不能为空") String destinationId);
}
