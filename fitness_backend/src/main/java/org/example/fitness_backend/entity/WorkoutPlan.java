package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("workout_plans")
public class WorkoutPlan {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private Long goalId;
    private String planName;
    private String planGoal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String frequency;
    private String description;
    private String status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}