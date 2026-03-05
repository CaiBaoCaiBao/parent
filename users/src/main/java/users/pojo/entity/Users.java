package users.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;

@Data
@TableName("users")
@Description(value = "用户信息表")
public class Users {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private String uUid; // ulid生成
    private String userName; // 前台用户自己输入，后台用户系统生成（全局唯一）
    private String email;
    private String password;
    private String role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("deleted")
    private Integer deleted;
}
