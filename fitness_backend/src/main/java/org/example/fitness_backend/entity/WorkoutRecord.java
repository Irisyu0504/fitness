package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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