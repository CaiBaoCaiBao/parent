package social.controller;

import common.enums.ResultCode;
import common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import social.pojo.dto.comment.CreateCommentDTO;
import social.pojo.dto.comment.DeleteCommentDTO;
import social.pojo.dto.comment.QueryCommentDTO;
import social.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/create")
    @Description(value = "创建评论")
    public Result<?> createComment(@RequestBody @Valid CreateCommentDTO dto) {
        return commentService.createComment(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/delete")
    @Description(value = "删除评论")
    public Result<?> deleteComment(@RequestBody @Valid DeleteCommentDTO dto) {
        log.info("deleteComment: {}", dto);
        return commentService.deleteComment(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/list")
    @Description(value = "查询评论列表")
    public Result<?> queryCommentList(@ModelAttribute @Valid QueryCommentDTO dto) {
        return commentService.queryCommentList(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/count")
    @Description(value = "获取评论总数")
    public Result<?> getCommentCount(){
        return commentService.getCommentCount();
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/monthly-count")
    @Description(value = "获取本月新增评论数")
    public Result<?> getMonthlyCommentCount(){
        return commentService.getMonthlyCommentCount();
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/last-monthly-count")
    @Description(value = "获取上月新增评论数")
    public Result<?> getLastMonthlyCommentCount(){
        return commentService.getLastMonthlyCommentCount();
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/monthly-count-by-month")
    @Description(value = "获取指定月份的新增评论数")
    public Result<?> getMonthlyCommentCountByMonth(@RequestParam("year") int year, @RequestParam("month") int month){
        return commentService.getMonthlyCommentCountByMonth(year, month);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/count-by-target")
    @Description(value = "获取单个目标的评论数")
    public Result<Integer> getCommentCountByTargetId(@RequestParam("targetId") String targetId){
        return commentService.getCommentCountByTargetId(targetId);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/batch-count")
    @Description(value = "批量获取评论数")
    public Result<Map<String, Integer>> getBatchCommentCount(@RequestParam("targetIds") List<String> targetIds) {
        return commentService.getBatchCommentCount(targetIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/reply")
    @Description(value = "回复评论")
    public Result<?> replyComment(@RequestBody @Valid CreateCommentDTO dto) {
        // 验证是否为回复评论（必须有父评论ID）
        if (dto.getParentCommentId() == null || dto.getParentCommentId().isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "回复评论必须指定父评论");
        }
        return commentService.createComment(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/api/delete-by-targets")
    @Description(value = "根据目标类型和目标ID列表批量删除评论")
    public Result<?> deleteCommentsByTargetIds(@RequestParam("targetType") String targetType, @RequestParam("targetIds") List<String> targetIds) {
        return commentService.deleteCommentsByTargetIds(targetType, targetIds);
    }
}
