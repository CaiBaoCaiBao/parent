package social.service;

import com.baomidou.mybatisplus.extension.service.IService;
import common.utils.Result;
import social.pojo.dto.comment.CreateCommentDTO;
import social.pojo.dto.comment.DeleteCommentDTO;
import social.pojo.dto.comment.QueryCommentDTO;
import social.pojo.entity.Comment;
import social.pojo.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Description;

@Description("评论管理")
public interface CommentService extends IService<Comment> {
    @Description("创建评论")
    Result<?> createComment(@Valid CreateCommentDTO dto);

    @Description("删除评论")
    Result<?> deleteComment(@Valid DeleteCommentDTO dto);

    @Description("查询评论列表")
    Result<?> queryCommentList(@Valid QueryCommentDTO dto);

    @Description("获取评论总数")
    Result<?> getCommentCount();
}
