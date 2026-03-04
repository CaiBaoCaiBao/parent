package users.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import users.pojo.entity.Users;

@Mapper
public interface AuthMapper extends BaseMapper<Users> {
}
