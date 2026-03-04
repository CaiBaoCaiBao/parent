package users.interceptor;

import common.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import users.mapper.UsersMapper;

/**
 * 用户上下文拦截器
 * 从 express-gateway 网关层传递的请求头中获取用户信息
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    /**
     * 网关传递的用户ID请求头
     */
    private static final String HEADER_USER_ID = "X-User-Id";

    /**
     * 网关传递的用户名请求头
     */
    private static final String HEADER_USERNAME = "X-Username";

    /**
     * 网关传递的邮箱请求头
     */
    private static final String HEADER_EMAIL = "X-Email";

    /**
     * 网关传递的昵称请求头
     */
    private static final String HEADER_NICKNAME = "X-Nickname";

    /**
     * 网关传递的头像请求头
     */
    private static final String HEADER_AVATAR = "X-Avatar";

    /**
     * 网关传递的角色请求头
     */
    private static final String HEADER_ROLE = "X-Role";

    /**
     * 网关传递的状态请求头
     */
    private static final String HEADER_STATUS = "X-Status";

    /**
     * 网关传递的设备ID请求头
     */
    private static final String HEADER_DEVICE_ID = "X-Device-Id";

    @Autowired
    private UsersMapper usersMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            // 从请求头获取用户信息
            String userIdStr = request.getHeader(HEADER_USER_ID);

            // 如果没有用户ID，说明是游客访问或未登录
            if (userIdStr == null || userIdStr.isEmpty()) {
                log.debug("请求未携带用户信息，游客访问");
                return true;
            }

            // 解析用户ID
            Long userId = parseLong(userIdStr);
            if (userId == null) {
                log.warn("无效的用户ID: {}", userIdStr);
                return true;
            }

            // 构建用户信息
            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setUserId(userId);
            userInfo.setUsername(request.getHeader(HEADER_USERNAME));
            userInfo.setEmail(request.getHeader(HEADER_EMAIL));
            userInfo.setNickname(request.getHeader(HEADER_NICKNAME));
            userInfo.setAvatar(request.getHeader(HEADER_AVATAR));
            userInfo.setRole(parseInteger(request.getHeader(HEADER_ROLE)));
            userInfo.setStatus(parseInteger(request.getHeader(HEADER_STATUS)));
            userInfo.setDeviceId(request.getHeader(HEADER_DEVICE_ID));

            // 设置到上下文
            UserContext.setUserInfo(userInfo);

            log.debug("用户上下文已设置: userId={}, username={}", userId, userInfo.getUsername());
        } catch (Exception e) {
            log.error("设置用户上下文失败", e);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求完成后清除上下文，避免内存泄漏
        UserContext.clear();
    }

    /**
     * 解析 Long 类型
     */
    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析 Integer 类型
     */
    private Integer parseInteger(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
