package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 健身目标实体类。
 * <p>对应数据库 fitness_goals 表，记录用户的健身目标设定，
 * 包括目标类型（减脂/增肌/塑形）、起止日期和每周目标等。</p>
 */
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
