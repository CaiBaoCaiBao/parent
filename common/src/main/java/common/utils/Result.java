package common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer code;
    private Boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> Result<T> success() {
        return new Result<>(200,true, "操作成功", null,LocalDateTime.now());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, true,"操作成功", data,LocalDateTime.now());
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200,true, message, data,LocalDateTime.now());
    }

    public static <T> Result<T> error() {
        return new Result<>(500,false, "操作失败", null,LocalDateTime.now());
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(500,false, message, null,LocalDateTime.now());
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code,false, message, null,LocalDateTime.now());
    }
}
