package users.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Description;
import users.pojo.dto.QueryUserListDTO;
import users.pojo.entity.Users;
import users.pojo.vo.UserInfoVo;
import users.pojo.vo.UserListVo;

import java.util.List;

@Mapper
public interface UsersMapper extends BaseMapper<Users> {
    @Description("查询用户列表")
    List<UserListVo> queryUserList(QueryUserListDTO queryUserListDTO);

    @Description("获取用户信息")
    UserInfoVo getUserInfo(String userName);

    @Description("通过用户名获取用户信息")
    UserInfoVo getUserInfoByUserName(String userName);

    @Description("批量获取用户信息")
    List<UserInfoVo> getBatchUserInfo(@Param("uids") List<String> uids);

    @Description("更新用户信息")
    int updateUser(Users users);

    UserInfoVo getUserInfoByUUid(String uUid);
}
