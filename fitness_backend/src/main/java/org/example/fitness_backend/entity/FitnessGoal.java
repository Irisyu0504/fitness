package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("fitness_goals")
public class FitnessGoal {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String goalType;
    private BigDecimal currentWeight;
    private BigDecimal targetWeight;
    private LocalDate startDate;
    private LocalDate targetDate;
    private BigDecimal weeklyTarget;
    private String status;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
