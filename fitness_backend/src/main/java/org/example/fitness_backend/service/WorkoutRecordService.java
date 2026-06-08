package org.example.fitness_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.fitness_backend.entity.WorkoutRecord;

import java.util.List;
import java.util.Map;

/**
 * 训练记录服务接口。
 * <p>管理用户的训练记录（运动项目、时长、组数、次数、消耗卡路里等），
 * 并提供分页查询、详情查询和肌群统计等高级查询能力。</p>
 */
public interface WorkoutRecordService extends IService<WorkoutRecord> {

    /**
     * 分页查询训练记录详情（含运动名称、肌群等关联信息）。
     * @param userId     用户 ID
     * @param recordDate 记录日期（可选，按日期筛选）
     * @param planId     训练计划 ID（可选，按计划筛选）
     * @param page       页码（从 1 开始）
     * @param size       每页条数
     * @return 包含分页数据和总条数的结果
     */
    Map<String, Object> getWorkoutRecordDetailsPaged(Long userId, String recordDate, String planId, int page, int size);

    /**
     * 查询训练记录详情列表（不分页，用于图表展示）。
     * @param userId     用户 ID
     * @param recordDate 记录日期（可选）
     * @param planId     训练计划 ID（可选）
     * @return 训练记录详情列表
     */
    List<Map<String, Object>> getWorkoutRecordDetails(Long userId, String recordDate, String planId);

    /**
     * 统计各肌群的训练次数，用于肌群分布分析。
     * @param userId 用户 ID
     * @return 各肌群及其训练次数的统计列表
     */
    List<Map<String, Object>> getMuscleStat(Long userId);
}
