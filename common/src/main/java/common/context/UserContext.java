package common.context;

import lombok.Data;

/**
 * 用户上下文
 * 使用 ThreadLocal 存储当前请求的用户信息
 */
public class UserContext {

    private static final ThreadLocal<UserInfo> USER_INFO = new ThreadLocal<>();

    /**
     * 设置用户信息
     */
    public static void setUserInfo(UserInfo userInfo) {
        USER_INFO.set(userInfo);
    }

    /**
     * 获取用户信息
     */
    public static UserInfo getUserInfo() {
        return USER_INFO.get();
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getUsername() : null;
    }

    /**
     * 获取邮箱
     */
    public static String getEmail() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getEmail() : null;
    }

    /**
     * 获取昵称
     */
    public static String getNickname() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getNickname() : null;
    }

    /**
     * 获取头像
     */
    public static String getAvatar() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getAvatar() : null;
    }

    /**
     * 获取角色
     */
    public static Integer getRole() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    /**
     * 获取状态
     */
    public static Integer getStatus() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getStatus() : null;
    }

    /**
     * 获取设备ID
     */
    public static String getDeviceId() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getDeviceId() : null;
    }

    /**
     * 清除用户信息
     */
    public static void clear() {
        USER_INFO.remove();
    }

    /**
     * 用户信息实体
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 头像
         */
        private String avatar;

        /**
         * 角色：0-普通用户，1-管理员
         */
        private Integer role;

        /**
         * 状态：0-禁用，1-正常
         */
        private Integer status;

        /**
         * 设备ID
         */
        private String deviceId;
    }
}
