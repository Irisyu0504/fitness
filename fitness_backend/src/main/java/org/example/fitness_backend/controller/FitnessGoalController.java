package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.entity.FitnessGoal;
import org.example.fitness_backend.entity.WorkoutPlan;
import org.example.fitness_backend.service.BodyRecordService;
import org.example.fitness_backend.service.FitnessGoalService;
import org.example.fitness_backend.service.WorkoutPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fitnessGoals")
public class FitnessGoalController {
    @Autowired
    private FitnessGoalService fitnessGoalService;

    @Autowired
    private WorkoutPlanService workoutPlanService;

    @Autowired
    private BodyRecordService bodyRecordService;

    @PostMapping
    public Result<String> addFitnessGoal(@RequestBody FitnessGoal fitnessGoal, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        fitnessGoal.setUserId(userId);

        // 若前端未传初始体重，自动取身体数据最新一条的体重
        if (fitnessGoal.getCurrentWeight() == null) {
            QueryWrapper<BodyRecord> latestBody = new QueryWrapper<>();
            latestBody.eq("user_id", userId).orderByDesc("record_date").last("LIMIT 1");
            BodyRecord latest = bodyRecordService.getOne(latestBody);
            if (latest != null && latest.getWeight() != null) {
                fitnessGoal.setCurrentWeight(latest.getWeight());
            }
        }

        if(fitnessGoal.getStartDate() != null && fitnessGoal.getTargetDate() != null && fitnessGoal.getTargetDate().isBefore(fitnessGoal.getStartDate())) {
            return Result.error(400, "目标完成日期不能早于开始日期");
        }

        // 保证同一时间只有一个目标
        QueryWrapper<FitnessGoal> updateWrapper = new QueryWrapper<>();
        updateWrapper.eq("user_id", userId).eq("status", "进行中");
        FitnessGoal oldGoal = new FitnessGoal();
        oldGoal.setStatus("已放弃");
        fitnessGoalService.update(oldGoal, updateWrapper);

        boolean success = fitnessGoalService.save(fitnessGoal);
        return success ? Result.success("健身目标创建成功", null) : Result.error(500, "创建失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateFitnessGoal(@PathVariable Long id, @RequestBody FitnessGoal fitnessGoal, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        if(fitnessGoal.getStartDate() != null && fitnessGoal.getTargetDate() != null && fitnessGoal.getTargetDate().isBefore(fitnessGoal.getStartDate())) {
            return Result.error(400, "目标完成日期不能早于开始日期");
        }

        FitnessGoal oldGoal = fitnessGoalService.getById(id);
        if (oldGoal == null || !oldGoal.getUserId().equals(userId)) {
            return Result.error(404, "目标不存在");
        }

        fitnessGoal.setUserId(userId);
        fitnessGoal.setId(id);

        boolean success = fitnessGoalService.updateById(fitnessGoal);
        return success ? Result.success("健身目标更新成功", null) : Result.error(500, "健身目标更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteFitnessGoal(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        FitnessGoal oldGoal = fitnessGoalService.getById(id);
        if (oldGoal == null || !oldGoal.getUserId().equals(userId)) {
            return Result.error(404, "目标不存在");
        }

        WorkoutPlan planUpdate = new WorkoutPlan();
        planUpdate.setGoalId(null);
        UpdateWrapper<WorkoutPlan> planWrapper = new UpdateWrapper<>();
        planWrapper.eq("user_id", userId).eq("goal_id", id).set("goal_id", null);
        workoutPlanService.update(planUpdate, planWrapper);

        boolean success = fitnessGoalService.removeById(id);
        return success ? Result.success("健身目标删除成功", null) : Result.error(500, "健身目标删除失败");
    }

    @GetMapping
    public Result<List<FitnessGoal>> getFitnessGoalList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<FitnessGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByAsc("create_time");

        List<FitnessGoal> list = fitnessGoalService.list(queryWrapper);
        return Result.success("查询趋势数据成功", list);
    }

    @GetMapping("/{id}")
    public Result<FitnessGoal> getFitnessGoalById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        FitnessGoal fitnessGoal = fitnessGoalService.getById(id);

        if (fitnessGoal == null || !fitnessGoal.getUserId().equals(userId)) {
            return Result.error(404, "目标不存在");
        }

        return Result.success("查询成功", fitnessGoal);
    }

    @GetMapping("/current")
    public Result<FitnessGoal> getCurrentFitnessGoal(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        QueryWrapper<FitnessGoal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("status", "进行中").orderByDesc("create_time").last("LIMIT 1");

        FitnessGoal fitnessGoal = fitnessGoalService.getOne(queryWrapper);
        return  Result.success("查询成功", fitnessGoal);
    }

    @GetMapping("/{id}/progress")
    public Result<Map<String, Object>> getFitnessGoalProgress(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        Map<String, Object> progressData = fitnessGoalService.calculateGoalProgress(userId);
        return Result.success("进度计算成功", progressData);
    }
}
