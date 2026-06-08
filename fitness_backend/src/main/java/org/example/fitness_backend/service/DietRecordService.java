package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.DietRecord;

import java.util.Map;

/**
 * 饮食记录服务接口。
 * <p>管理用户的饮食记录（食物名称、餐次、热量及营养素），
 * 并提供当日卡路里统计能力。</p>
 */
public interface DietRecordService extends IService<DietRecord> {

    /**
     * 统计指定用户当日的卡路里摄入总量和各餐次分布。
     * @param userId 用户 ID
     * @return 包含当日总热量和餐次明细的统计结果
     */
    Map<String, Object> getTodayCalorieStat(Long userId);
}
