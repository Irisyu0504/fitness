package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.WorkoutPlan;
import org.example.fitness_backend.service.WorkoutPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workout-plans")
public class WorkoutPlanController {
    @Autowired
    private WorkoutPlanService workoutPlanService;

    @PostMapping
    public Result<String> addWorkoutPlan(@RequestBody WorkoutPlan workoutPlan, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        workoutPlan.setUserId(userId);

        if(workoutPlan.getPlanGoal() == null || workoutPlan.getPlanName() == null) {
            return Result.error(400, "健身计划目标和健身计划名字不能为空");
        }

        if(workoutPlan.getStartDate() != null && workoutPlan.getEndDate() != null
                && workoutPlan.getStartDate().isAfter(workoutPlan.getEndDate())) {
            return Result.error(400, "计划开始日期不能晚于结束日期");
        }

        boolean success = workoutPlanService.save(workoutPlan);
        return success ? Result.success("健身计划记录成功", null) : Result.error(500, "健身计划记录失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateWorkoutPlan(@PathVariable Long id, @RequestBody WorkoutPlan workoutPlan, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UpdateWrapper<WorkoutPlan> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("user_id", userId);

        boolean success = workoutPlanService.update(workoutPlan, updateWrapper);
        if (!success) {
            return Result.error(403, "修改失败，可能是计划不存在或您无权修改别人的计划");
        }
        return Result.success("健身计划修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteWorkoutPlan(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<WorkoutPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("user_id", userId);

        boolean success = workoutPlanService.remove(queryWrapper);
        if (!success) {
            return Result.error(403, "删除失败，记录不存在或无权删除");
        }
        return Result.success("健身计划删除成功", null);
    }

    @GetMapping
    public Result<List<WorkoutPlan>> getWorkoutPlanList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<WorkoutPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");

        List<WorkoutPlan> list = workoutPlanService.list(queryWrapper);
        return Result.success("健身计划查询成功", list);
    }

    @GetMapping("/{id}")
    public Result<WorkoutPlan> getWorkoutPlanDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<WorkoutPlan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("user_id", userId);

        WorkoutPlan workoutPlan = workoutPlanService.getOne(queryWrapper);

        if(workoutPlan == null) {
            return Result.error(404, "健身计划不存在或无权查看");
        }
        return Result.success("健身计划查询成功", workoutPlan);
    }
}
