package org.example.fitness_backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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