package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.service.BodyRecordService;
import org.example.fitness_backend.service.DietRecordService;
import org.example.fitness_backend.service.FitnessGoalService;
import org.example.fitness_backend.service.WorkoutRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stat")
public class StatController {

    @Autowired
    private FitnessGoalService fitnessGoalService;
    @Autowired
    private DietRecordService dietRecordService;
    @Autowired
    private WorkoutRecordService workoutRecordService;
    @Autowired
    private BodyRecordService bodyRecordService;

    // 1. 首页仪表盘数据 (综合大满贯)
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> dashboard = new HashMap<>();

        // 模块A：获取目标进度
        Map<String, Object> goalProgress = fitnessGoalService.calculateGoalProgress(userId);
        dashboard.put("goalInfo", goalProgress);

        // 模块B：获取今日饮食热量
        Map<String, Object> dietStat = dietRecordService.getTodayCalorieStat(userId);
        BigDecimal dietCalories = (BigDecimal) dietStat.get("totalCalories");
        dashboard.put("todayDietCalories", dietCalories);

        // 模块C：获取今日训练消耗和时长 (复用之前的 muscleStat 方法稍微处理，或直接简单查库)
        // 这里为了简单直观，直接统计今天的训练
        String todayStr = LocalDate.now().toString();
        List<Map<String, Object>> todayWorkouts = workoutRecordService.getWorkoutRecordDetails(userId, todayStr, null);

        int todayWorkoutDuration = 0;
        BigDecimal todayWorkoutCalories = BigDecimal.ZERO;
        for (Map<String, Object> map : todayWorkouts) {
            Object duration = map.get("duration");
            Object calories = map.get("caloriesBurned");
            if (duration != null) todayWorkoutDuration += ((Number) duration).intValue();
            if (calories != null) todayWorkoutCalories = todayWorkoutCalories.add(new BigDecimal(calories.toString()));
        }

        dashboard.put("todayWorkoutDuration", todayWorkoutDuration);
        dashboard.put("todayWorkoutCalories", todayWorkoutCalories);

        // 模块D：计算今日热量差值 = 摄入 - 消耗
        BigDecimal calorieBalance = dietCalories.subtract(todayWorkoutCalories);
        dashboard.put("calorieBalance", calorieBalance);

        return Result.success("仪表盘数据加载成功", dashboard);
    }

    // 2. 体重趋势统计 (复用 BodyRecordService)
    @GetMapping("/weight-trend")
    public Result<List<BodyRecord>> getWeightTrend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 获取最近30天的身体数据，正序排列供折线图使用
        java.time.LocalDate startDate = java.time.LocalDate.now().minusDays(30);
        QueryWrapper<BodyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).ge("record_date", startDate).orderByAsc("record_date");
        List<BodyRecord> records = bodyRecordService.list(queryWrapper);
        records.forEach(bodyRecordService::calculateAndSetBmi);
        return Result.success("体重趋势获取成功", records);
    }

    // 3. BMI 趋势统计 (其实前端拿 weight-trend 的数据也能画，但为了严格符合文档，独立给一个)
    @GetMapping("/bmi-trend")
    public Result<List<BodyRecord>> getBmiTrend(HttpServletRequest request) {
        // 数据源和体重趋势是一样的，前端 ECharts 取数据时取 .bmi 字段即可
        return getWeightTrend(request);
    }

    // 4. 热量收支统计
    @GetMapping("/calorie-balance")
    public Result<Map<String, Object>> getCalorieBalance(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("热量收支统计成功", dietRecordService.getTodayCalorieStat(userId));
    }

    // 5. 训练部位占比统计
    @GetMapping("/muscle-distribution")
    public Result<List<Map<String, Object>>> getMuscleDistribution(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("训练部位占比统计成功", workoutRecordService.getMuscleStat(userId));
    }

    // 6. 周训练时长统计 (预留给图表：过去7天每天练了多久)
    @GetMapping("/workout-duration")
    public Result<List<Map<String, Object>>> getWorkoutDurationTrend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        // 核心思路：取最近7天的 workout_records，按日期 Group By 算 duration 的 sum
        // 实际开发中，可以让前端去 /api/workout-records 自己组装，或者在这里写一段 Stream 分组代码。
        // 这里提供最简单的实现思路：调取详情列表，由前端按日期分组聚合。
        List<Map<String, Object>> recentWorkouts = workoutRecordService.getWorkoutRecordDetails(userId, null, null);
        return Result.success("周训练时长数据获取成功", recentWorkouts);
    }
}
