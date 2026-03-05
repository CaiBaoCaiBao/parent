package common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 参数校验失败
     */
    VALIDATE_FAILED(400, "参数校验失败"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 请求实体过大
     */
    PAYLOAD_TOO_LARGE(413, "请求实体过大"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    /**
     * 内部服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    /**
     * 网关超时
     */
    GATEWAY_TIMEOUT(504, "网关超时"),

    /**
     * Token无效或已过期
     */
    TOKEN_INVALID(1001, "Token无效或已过期"),

    /**
     * Token解析失败
     */
    TOKEN_PARSE_ERROR(1002, "Token解析失败"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(2001, "用户不存在"),

    /**
     * 用户已存在
     */
    USER_ALREADY_EXISTS(2002, "用户已存在"),

    /**
     * 密码错误
     */
    PASSWORD_ERROR(2003, "密码错误"),

    /**
     * 账号已被禁用
     */
    ACCOUNT_DISABLED(2004, "账号已被禁用"),

    /**
     * 账号已被锁定
     */
    ACCOUNT_LOCKED(2005, "账号已被锁定"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(3001, "文件上传失败"),

    /**
     * 文件类型不支持
     */
    FILE_TYPE_NOT_SUPPORTED(3002, "文件类型不支持"),

    /**
     * 文件大小超限
     */
    FILE_SIZE_EXCEEDED(3003, "文件大小超限"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(4001, "数据已存在"),

    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(4002, "数据不存在"),

    /**
     * 数据操作失败
     */
    DATA_OPERATION_FAILED(4003, "数据操作失败"),

    /**
     * 缓存操作失败
     */
    CACHE_OPERATION_FAILED(5001, "缓存操作失败"),

    /**
     * 数据库操作失败
     */
    DATABASE_OPERATION_FAILED(5002, "数据库操作失败"),

    /**
     * 远程调用失败
     */
    REMOTE_CALL_FAILED(6001, "远程调用失败"),

    /**
     * 系统繁忙
     */
    SYSTEM_BUSY(9999, "系统繁忙，请稍后再试");

    private final Integer code;
    private final String message;
}
