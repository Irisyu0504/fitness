package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运动动作实体类。
 * <p>对应数据库 exercises 表，存储运动库的基础数据，
 * 包括动作名称、目标肌群、难度等级和每分钟消耗热量等信息。</p>
 */
@Data
@TableName("exercises")
public class Exercise {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String exerciseName;  // 动作名称
    private String muscleGroup;   // 训练部位
    private String difficulty;    // 难度
    private BigDecimal caloriesPerMinute; // 每分钟消耗
    private String description;   // 描述
    private String imageUrl;      // 图片地址
    private Integer status;       // 状态 1启用 0禁用

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}