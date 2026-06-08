package org.example.fitness_backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.fitness_backend.entity.WorkoutRecord;

import java.util.List;
import java.util.Map;

@Mapper
public interface WorkoutRecordMapper extends BaseMapper<WorkoutRecord> {

    /** 多表连接查询：训练记录 JOIN 运动动作 JOIN 训练计划 */
    List<Map<String, Object>> getRecordDetailByUserId(
            @Param("userId") Long userId,
            @Param("recordDate") String recordDate,
            @Param("planId") String planId
    );

    /** 按肌群统计（JOIN + GROUP BY 聚合） */
    List<Map<String, Object>> getMuscleStatByUserId(@Param("userId") Long userId);
}