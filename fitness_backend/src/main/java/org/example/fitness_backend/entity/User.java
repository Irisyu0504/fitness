package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类。
 * <p>对应数据库 users 表，存储用户的基本信息、认证信息和会员状态。</p>
 * <p>ID 策略为雪花算法（ASSIGN_ID），保证分布式环境下的唯一性。</p>
 */
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
