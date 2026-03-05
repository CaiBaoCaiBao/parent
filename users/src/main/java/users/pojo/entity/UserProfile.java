package users.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.context.annotation.Description;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
@Description(value = "用户资料")
public class UserProfile {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long uid;
    private String nickName; // 昵称
    private String idCard; // 身份证
    private String phone; // 手机号
    private String avatar; // 头像
    private String bio; // 个人简介
    private LocalDate birthDay; // 生日
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableField("deleted")
    private Integer deleted;
}
