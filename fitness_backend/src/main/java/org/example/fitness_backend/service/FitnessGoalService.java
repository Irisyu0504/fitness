package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.FitnessGoal;

import java.util.Map;

/**
 * 健身目标服务接口。
 * <p>管理用户的健身目标（减脂、增肌、塑形等），
 * 并提供目标进度计算能力。</p>
 */
public interface FitnessGoalService extends IService<FitnessGoal> {

    /**
     * 计算指定用户的健身目标完成进度。
     * <p>根据当前体重与目标体重的差距、已用时间与总时间的比例等维度综合计算。</p>
     * @param userId 用户 ID
     * @return 包含进度百分比、剩余天数等信息的统计结果
     */
    Map<String, Object> calculateGoalProgress(Long userId);
}
