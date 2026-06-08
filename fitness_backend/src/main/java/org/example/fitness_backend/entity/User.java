package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String email;
    private String gender;
    private BigDecimal height;
    private String avatar;
    private String role;
    private Boolean status;
    private LocalDateTime vipExpireTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
