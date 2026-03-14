package social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import social.pojo.entity.Comment;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
