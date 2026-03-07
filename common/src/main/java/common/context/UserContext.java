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
    public static String getUserUUid() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getUUid() : null;
    }

    /**
     * 获取用户名
     */
    public static String getUsername() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getUserName() : null;
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
        return userInfo != null ? userInfo.getNickName() : null;
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
    public static String getRole() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getRole() : null;
    }

    /**
     * 获取状态
     */
    public static String getStatus() {
        UserInfo userInfo = USER_INFO.get();
        return userInfo != null ? userInfo.getStatus() : null;
    }

//    /**
//     * 获取设备ID
//     */
//    public static String getDeviceId() {
//        UserInfo userInfo = USER_INFO.get();
//        return userInfo != null ? userInfo.getDeviceId() : null;
//    }

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
        private String uUid;

        /**
         * 用户名
         */
        private String userName;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 昵称
         */
        private String nickName;

        /**
         * 头像
         */
        private String avatar;

        /**
         * 角色：user-普通用户，admin-管理员
         */
        private String role;

        /**
         * 状态：inactive-禁用，active-正常
         */
        private String status;

        /**
         * 设备ID
         */
//        private String deviceId;
    }
}
