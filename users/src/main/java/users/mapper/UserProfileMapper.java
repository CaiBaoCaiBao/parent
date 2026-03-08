package users.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import users.pojo.entity.UserProfile;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
    int updateByUid(UserProfile userProfile);
}
