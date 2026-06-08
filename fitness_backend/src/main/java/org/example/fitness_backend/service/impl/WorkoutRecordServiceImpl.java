package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.fitness_backend.entity.Exercise;
import org.example.fitness_backend.entity.WorkoutRecord;
import org.example.fitness_backend.mapper.ExerciseMapper;
import org.example.fitness_backend.mapper.WorkoutRecordMapper;
import org.example.fitness_backend.service.WorkoutRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkoutRecordServiceImpl extends ServiceImpl<WorkoutRecordMapper, WorkoutRecord> implements WorkoutRecordService {
    @Autowired
    private ExerciseMapper exerciseMapper;
    @Autowired
    private WorkoutRecordMapper workoutRecordMapper;

    @Override
    public boolean save(WorkoutRecord workoutRecord) {
        if (workoutRecord.getCaloriesBurned() == null && workoutRecord.getExerciseId() != null && workoutRecord.getDuration() != null) {
            Exercise exercise = exerciseMapper.selectById(workoutRecord.getExerciseId());
            if (exercise != null && exercise.getCaloriesPerMinute() != null) {
                // 热量 = 每分钟消耗 * 时长
                BigDecimal durationBd = new BigDecimal(workoutRecord.getDuration());
                BigDecimal totalCalories = exercise.getCaloriesPerMinute().multiply(durationBd);
                workoutRecord.setCaloriesBurned(totalCalories);
            } else {
                workoutRecord.setCaloriesBurned(BigDecimal.ZERO); // 如果动作库没有参考热量，给个默认值0
            }
        }
        workoutRecord.setCreateTime(LocalDateTime.now());
        workoutRecord.setUpdateTime(LocalDateTime.now());
        return super.save(workoutRecord);
    }

    @Override
    public boolean updateById(WorkoutRecord workoutRecord) {
        workoutRecord.setUpdateTime(LocalDateTime.now());
        return super.updateById(workoutRecord);
    }

    // 多表连接查询（分页）— 使用 SQL LEFT JOIN 替代 N+1 查询
    @Override
    public Map<String, Object> getWorkoutRecordDetailsPaged(Long userId, String recordDate, String planId, int page, int size) {
        // 先用 JOIN 查出该用户的全部记录详情
        List<Map<String, Object>> allDetails = workoutRecordMapper.getRecordDetailByUserId(userId, recordDate, planId);

        // 手动分页
        int total = allDetails.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(page * size, total);
        List<Map<String, Object>> pageData = allDetails.subList(from, to);

        // 补充 planName 默认值
        for (Map<String, Object> map : pageData) {
            if (map.get("planName") == null) {
                map.put("planName", "自由训练");
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", pageData);
        result.put("total", total);
        return result;
    }

    // 多表连接查询（不分页）— SQL LEFT JOIN
    @Override
    public List<Map<String, Object>> getWorkoutRecordDetails(Long userId, String recordDate, String planId) {
        List<Map<String, Object>> details = workoutRecordMapper.getRecordDetailByUserId(userId, recordDate, planId);
        for (Map<String, Object> map : details) {
            if (map.get("planName") == null) {
                map.put("planName", "自由训练");
            }
        }
        return details;
    }

    // 训练部位统计分析 — SQL JOIN + GROUP BY 聚合
    @Override
    public List<Map<String, Object>> getMuscleStat(Long userId) {
        return workoutRecordMapper.getMuscleStatByUserId(userId);
    }
}
