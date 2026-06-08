package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.Exercise;

/**
 * 运动库服务接口。
 * <p>管理运动动作库（名称、目标肌群、难度、每分钟消耗等），
 * 为训练记录提供运动项目的基础数据支撑。</p>
 */
public interface ExerciseService extends IService<Exercise> {
}
