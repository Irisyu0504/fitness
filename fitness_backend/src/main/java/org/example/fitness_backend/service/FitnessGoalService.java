package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.FitnessGoal;

import java.util.Map;

public interface FitnessGoalService extends IService<FitnessGoal>{
    Map<String, Object> calculateGoalProgress(Long userId);
}
