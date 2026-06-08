package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 训练记录实体类。
 * <p>对应数据库 workout_records 表，记录用户每次训练的详细数据，
 * 包括运动项目、时长、组数、次数和消耗卡路里等信息。</p>
 */
@Data
@TableName("workout_records")
public class WorkoutRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long planId;
    private Long exerciseId;
    private Integer duration; //运动时长
    private Integer setsCount;
    private Integer reps; //每组次数
    private BigDecimal caloriesBurned;
    private LocalDate recordDate; //记录日期
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}