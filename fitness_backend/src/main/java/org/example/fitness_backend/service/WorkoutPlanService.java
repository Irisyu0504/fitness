package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.WorkoutPlan;

/**
 * 训练计划服务接口。
 * <p>管理用户的训练计划（计划名称、目标、频率、周期等），
 * 计划可关联健身目标，并作为训练记录的分组依据。</p>
 */
public interface WorkoutPlanService extends IService<WorkoutPlan> {
}
