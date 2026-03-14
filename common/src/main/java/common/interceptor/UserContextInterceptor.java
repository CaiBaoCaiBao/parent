package common.interceptor;

import common.context.UserContext;
import common.utils.Verification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器
 * 从 express-gateway 网关层传递的请求头中获取用户信息
 */
@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {
    @Autowired
    Verification verification;

    /**
     * 网关传递的用户ID请求头
     */
    private static final String HEADER_USER_ID = "X-User-Uuid";

    /**
     * 网关传递的用户名请求头
     */
    private static final String HEADER_USERNAME = "X-User-Username";

    /**
     * 网关传递的邮箱请求头
     */
    private static final String HEADER_EMAIL = "X-User-Email";

    /**
     * 网关传递的昵称请求头
     */
    private static final String HEADER_NICKNAME = "X-User-Nickname";

    /**
     * 网关传递的头像请求头
     */
    private static final String HEADER_AVATAR = "X-User-Avatar";

    /**
     * 网关传递的角色请求头
     */
    private static final String HEADER_ROLE = "X-User-Role";

    /**
     * 网关传递的状态请求头
     */
    private static final String HEADER_STATUS = "X-User-Status";

//    /**
//     * 网关传递的设备ID请求头
//     */
//    private static final String HEADER_DEVICE_ID = "X-Device-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String requestUri = request.getRequestURI();
            log.debug("处理请求: {}", requestUri);

            // 从请求头获取用户信息
            String userUid = request.getHeader(HEADER_USER_ID);
            String userName = request.getHeader(HEADER_USERNAME);
            String email = request.getHeader(HEADER_EMAIL);
            String nickName = request.getHeader(HEADER_NICKNAME);
            String avatar = request.getHeader(HEADER_AVATAR);
            String role = request.getHeader(HEADER_ROLE);
            String status = request.getHeader(HEADER_STATUS);

            log.debug("请求头信息 - X-User-Uuid: {}, X-User-Username: {}, X-User-Role: {}",
                    userUid, userName, role);

            // 如果没有用户ID，说明是游客访问或未登录
            if (userUid == null || userUid.isEmpty()) {
                log.debug("请求未携带用户信息，游客访问: {}", requestUri);
                return true;
            }

            // 解析用户ID
            String uUid = verification.trimStr(userUid);
            if (uUid == null) {
                log.warn("无效的用户ID: {}", userUid);
                return true;
            }

            // 构建用户信息
            UserContext.UserInfo userInfo = new UserContext.UserInfo();
            userInfo.setUUid(userUid);
            userInfo.setUserName(decodeHeader(userName));
            userInfo.setEmail(email);
            userInfo.setNickName(decodeHeader(nickName));
            userInfo.setAvatar(avatar);
            userInfo.setRole(role);
            userInfo.setStatus(status);
//            userInfo.setDeviceId(request.getHeader(HEADER_DEVICE_ID));

            // 设置到上下文
            UserContext.setUserInfo(userInfo);

            log.debug("用户上下文已设置: uUid={}, username={}, role={}", uUid, userInfo.getUserName(), userInfo.getRole());
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

    /**
     * 解码请求头（处理中文等非 ASCII 字符）
     * 使用 Base64 解码
     */
    private String decodeHeader(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            // 尝试 Base64 解码
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(value);
            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            // 如果解码失败，返回原始值
            log.debug("解码请求头失败: {}, 使用原始值", value);
            return value;
        }
    }
}
