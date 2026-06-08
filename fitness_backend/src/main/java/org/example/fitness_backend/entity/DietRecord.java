package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 饮食记录实体类。
 * <p>对应数据库 diet_records 表，记录用户的每餐饮食信息，
 * 包括食物名称、餐次类型、热量及三大营养素（蛋白质、碳水、脂肪）。</p>
 */
@Data
@TableName("diet_records")
public class DietRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private String foodName;
    private String mealType;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbs;
    private BigDecimal fat;
    private LocalDateTime mealTime;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}