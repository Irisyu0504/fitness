package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.DietRecord;

import java.util.Map;

public interface DietRecordService extends IService<DietRecord> {
    Map<String, Object> getTodayCalorieStat(Long userId);
}
