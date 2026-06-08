package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.WorkoutRecord;

import java.util.List;
import java.util.Map;

public interface WorkoutRecordService extends IService<WorkoutRecord> {

    Map<String, Object> getWorkoutRecordDetailsPaged(Long userId, String recordDate, String planId, int page, int size);

    List<Map<String, Object>> getWorkoutRecordDetails(Long userId, String recordDate, String planId);

    List<Map<String, Object>> getMuscleStat(Long userId);
}
