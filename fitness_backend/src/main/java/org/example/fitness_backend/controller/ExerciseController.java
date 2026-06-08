package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.Exercise;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.service.ExerciseService;
import org.example.fitness_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {
    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private UserService userService;

    private boolean isAdmin(Long userId) {
        User user = userService.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    @PostMapping
    public Result<String> addExercise(@RequestBody Exercise exercise, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if(!isAdmin(userId)) {
            return Result.error(403, "越权操作:只有管理员才能新增动作库");
        }

        if(exercise.getExerciseName() == null || exercise.getMuscleGroup() == null) {
            return Result.error(400, "动作名称和训练部位不能为空");
        }

        boolean success = exerciseService.save(exercise);
        return success ? Result.success("新增动作成功", null) : Result.error(500, "新增失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateExercise(@PathVariable Long id, @RequestBody Exercise exercise, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if(!isAdmin(userId)) {
            return Result.error(403, "越权操作:只有管理员才能新增动作库");
        }

        exercise.setId(id);
        boolean success = exerciseService.updateById(exercise);
        return success ? Result.success("动作更新成功", null) : Result.error(500, "动作修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteExercise(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if(!isAdmin(userId)) {
            return Result.error(403, "越权操作:只有管理员才能新增动作库");
        }

        boolean success = exerciseService.removeById(id);
        return success ? Result.success("删除成功", null) : Result.error(500, "删除失败");
    }

    @GetMapping
    public Result<?> getExerciseList(
            @RequestParam(required = false) String exerciseName,
            @RequestParam(required = false) String muscleGroup,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        QueryWrapper<Exercise> queryWrapper = new QueryWrapper<>();

        // 只查询启用状态的运动
        queryWrapper.eq("status", 1);

        if (exerciseName != null && !exerciseName.isEmpty()) {
            queryWrapper.like("exercise_name", exerciseName);
        }
        if (muscleGroup != null && !muscleGroup.isEmpty()) {
            queryWrapper.eq("muscle_group", muscleGroup);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            queryWrapper.eq("difficulty", difficulty);
        }
        queryWrapper.orderByDesc("create_time");

        if (page != null && size != null) {
            Page<Exercise> pageResult = exerciseService.page(new Page<>(page, size), queryWrapper);
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            return Result.success("查询成功", result);
        }
        return Result.success("健身动作查询成功", exerciseService.list(queryWrapper));
    }

    @GetMapping("/{id}")
    public Result<Exercise> getExerciseDetail(@PathVariable Long id) {
        Exercise exercise = exerciseService.getById(id);

        if(exercise == null) {
            return Result.error(400, "健身动作不存在");
        }
        return Result.success("健身动作查询成功", exercise);
    }

}
