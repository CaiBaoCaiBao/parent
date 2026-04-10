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

import java.util.List;
import java.util.Map;

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

    @Description("获取本月新增评论数")
    Result<?> getMonthlyCommentCount();

    @Description("获取上月新增评论数")
    Result<?> getLastMonthlyCommentCount();

    @Description("获取指定月份的新增评论数")
    Result<?> getMonthlyCommentCountByMonth(int year, int month);

    @Description("获取单个目标的评论数")
    Result<Integer> getCommentCountByTargetId(String targetId);

    @Description("批量获取评论数")
    Result<Map<String, Integer>> getBatchCommentCount(List<String> targetIds);

    @Description("根据目标类型和目标ID列表批量删除评论")
    Result<?> deleteCommentsByTargetIds(String targetType, List<String> targetIds);
}
