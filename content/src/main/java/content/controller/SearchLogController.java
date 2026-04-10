package content.controller;

import common.context.UserContext;
import common.utils.Result;
import content.pojo.dto.search.HotKeywordsDTO;
import content.pojo.dto.search.SaveSearchLogDTO;
import content.service.SearchLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search-log")
public class SearchLogController {

    @Autowired
    private SearchLogService searchLogService;

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/api/save")
    @Description(value = "保存搜索记录")
    public Result<?> saveSearchLog(@RequestBody @Valid SaveSearchLogDTO dto, HttpServletRequest request) {
        // 设置IP地址
        String ipAddress = getClientIpAddress(request);
        dto.setIpAddress(ipAddress);

        // 设置用户代理
        String userAgent = request.getHeader("User-Agent");
        dto.setUserAgent(userAgent);

        return searchLogService.saveSearchLog(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/hot-keywords")
    @Description(value = "获取热门搜索关键词")
    public Result<?> getHotKeywords(@Valid @ModelAttribute HotKeywordsDTO dto) {
        return searchLogService.getHotKeywords(dto);
    }

    @Transactional(rollbackFor = Exception.class)
    @GetMapping("/api/user-history")
    @Description(value = "获取用户搜索历史")
    public Result<?> getUserSearchHistory(@RequestParam(defaultValue = "10") int limit) {
        String userId = UserContext.getUserUUid();
        if (userId == null || userId.isEmpty()) {
            return Result.error("用户未登录");
        }
        return searchLogService.getUserSearchHistory(userId, limit);
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
