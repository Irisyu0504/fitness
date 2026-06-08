package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体测记录实体类。
 * <p>对应数据库 body_records 表，记录用户每次体测的身体数据，
 * 包括体重、体脂率、腰围和 BMI 等指标。</p>
 */
@Data
@TableName("body_records")
public class BodyRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;
    private BigDecimal weight;
    private BigDecimal bodyFatRate;
    private BigDecimal waistline;
    private BigDecimal bmi;
    private LocalDate recordDate;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}