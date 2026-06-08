package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.fitness_backend.entity.WorkoutPlan;
import org.example.fitness_backend.mapper.WorkoutPlanMapper;
import org.example.fitness_backend.service.WorkoutPlanService;
import org.springframework.stereotype.Service;

@Service
public class WorkoutPlanServiceImpl extends ServiceImpl<WorkoutPlanMapper, WorkoutPlan> implements WorkoutPlanService {
}
