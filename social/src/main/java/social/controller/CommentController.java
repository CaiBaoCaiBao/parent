package social.controller;

import common.utils.Result;
import social.pojo.dto.comment.CreateCommentDTO;
import social.pojo.dto.comment.DeleteCommentDTO;
import social.pojo.dto.comment.QueryCommentDTO;
import social.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    @DeleteMapping("/api/delete")
    @Description(value = "删除评论")
    public Result<?> deleteComment(@RequestBody @Valid DeleteCommentDTO dto) {
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
}
