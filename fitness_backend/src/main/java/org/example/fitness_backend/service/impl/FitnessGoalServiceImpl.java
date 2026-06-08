package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.entity.FitnessGoal;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.mapper.BodyRecordMapper;
import org.example.fitness_backend.mapper.FitnessGoalMapper;
import org.example.fitness_backend.mapper.UserMapper;
import org.example.fitness_backend.service.FitnessGoalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class FitnessGoalServiceImpl extends ServiceImpl<FitnessGoalMapper, FitnessGoal> implements FitnessGoalService {

    @Autowired
    private FitnessGoalMapper fitnessGoalMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BodyRecordMapper bodyRecordMapper;

    @Override
    public boolean save(FitnessGoal fitnessGoal) {
        fitnessGoal.setStatus("进行中");
        fitnessGoal.setCreateTime(LocalDateTime.now());
        fitnessGoal.setUpdateTime(LocalDateTime.now());
        return super.save(fitnessGoal);
    }

    @Override
    public boolean updateById(FitnessGoal fitnessGoal) {
        fitnessGoal.setUpdateTime(LocalDateTime.now());
        return  super.updateById(fitnessGoal);
    }

    @Override
    public Map<String, Object> calculateGoalProgress(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 查找用户当前“进行中”的目标
        QueryWrapper<FitnessGoal> goalQuery = new QueryWrapper<>();
        goalQuery.eq("user_id", userId).eq("status", "进行中").orderByDesc("create_time").last("LIMIT 1");
        FitnessGoal currentGoal = this.getOne(goalQuery);

        if (currentGoal == null) {
            result.put("hasGoal", false);
            result.put("msg", "暂无进行中的健身目标");
            return result;
        }

        result.put("hasGoal", true);
        result.put("goal", currentGoal);

        // 2. 查找用户最新的一条体重记录
        QueryWrapper<BodyRecord> bodyQuery = new QueryWrapper<>();
        bodyQuery.eq("user_id", userId).orderByDesc("record_date", "create_time").last("LIMIT 1");
        BodyRecord latestRecord = bodyRecordMapper.selectOne(bodyQuery);

        BigDecimal latestWeight = latestRecord != null ? latestRecord.getWeight() : currentGoal.getCurrentWeight();
        result.put("latestWeight", latestWeight);

        // 3. 计算进度差值
        BigDecimal initialWeight = currentGoal.getCurrentWeight();
        BigDecimal targetWeight = currentGoal.getTargetWeight();

        // 减脂/增肌：目标体重和初始体重一样则进度为 0，防止除以 0
        boolean isWeightGoal = "减脂".equals(currentGoal.getGoalType()) || "增肌".equals(currentGoal.getGoalType());
        if (isWeightGoal && initialWeight.compareTo(targetWeight) == 0) {
            result.put("progressRate", 0);
            return result;
        }

        BigDecimal progress = BigDecimal.ZERO;
        // 减脂逻辑
        if ("减脂".equals(currentGoal.getGoalType())) {
            // (初始 - 最新) / (初始 - 目标)
            BigDecimal weightLost = initialWeight.subtract(latestWeight);
            BigDecimal totalToLose = initialWeight.subtract(targetWeight);
            progress = weightLost.divide(totalToLose, 4, RoundingMode.HALF_UP);
        }
        // 增肌逻辑
        else if ("增肌".equals(currentGoal.getGoalType())) {
            // (最新 - 初始) / (目标 - 初始)
            BigDecimal weightGained = latestWeight.subtract(initialWeight);
            BigDecimal totalToGain = targetWeight.subtract(initialWeight);
            progress = weightGained.divide(totalToGain, 4, RoundingMode.HALF_UP);
        }
        // 维持体重/提高体能：按时间进度计算
        else {
            if (currentGoal.getStartDate() != null && currentGoal.getTargetDate() != null) {
                long totalDays = java.time.temporal.ChronoUnit.DAYS.between(currentGoal.getStartDate(), currentGoal.getTargetDate());
                long passedDays = java.time.temporal.ChronoUnit.DAYS.between(currentGoal.getStartDate(), java.time.LocalDate.now());
                if (totalDays > 0) {
                    progress = new BigDecimal(passedDays).divide(new BigDecimal(totalDays), 4, RoundingMode.HALF_UP);
                }
            }
        }

        // 处理进度范围（防止超出0%~100%）
        double percentage = progress.doubleValue() * 100;
        if (percentage < 0) percentage = 0;
        if (percentage > 100) percentage = 100;

        result.put("progressRate", Math.round(percentage)); // 返回百分比整数，例如 65 代表 65%
        return result;
    }
}
