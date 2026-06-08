package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.WorkoutRecord;
import org.example.fitness_backend.service.WorkoutRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workout-records")
public class WorkoutRecordController {
    @Autowired
    private WorkoutRecordService workoutRecordService;

    @PostMapping
    public Result<String> addWorkoutRecords(@RequestBody WorkoutRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        record.setUserId(userId);

        if (record.getExerciseId() == null || record.getRecordDate() == null || record.getDuration() == null) {
            return Result.error(400, "动作、训练日期、训练时长不能为空");
        }

        boolean success = workoutRecordService.save(record);
        return success ? Result.success("训练记录保存成功", null) : Result.error(500, "保存失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateRecord(@PathVariable Long id, @RequestBody WorkoutRecord record, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        UpdateWrapper<WorkoutRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("user_id", userId);

        boolean success = workoutRecordService.update(record, updateWrapper);
        return success ? Result.success("修改成功", null) : Result.error(403, "修改失败，记录不存在或无权修改");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<WorkoutRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("user_id", userId);

        boolean success = workoutRecordService.remove(queryWrapper);
        return success ? Result.success("删除成功", null) : Result.error(403, "删除失败，记录不存在或无权删除");
    }

    @GetMapping
    public Result<?> getRecordList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        QueryWrapper<WorkoutRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("record_date");

        if (page != null && size != null) {
            Page<WorkoutRecord> pageResult = workoutRecordService.page(new Page<>(page, size), queryWrapper);
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            return Result.success("查询成功", result);
        }
        return Result.success("查询成功", workoutRecordService.list(queryWrapper));
    }

    // 训练记录详情 (多表组装，返回动作名、部位、计划名等)
    @GetMapping("/detail")
    public Result<?> getRecordDetail(
            @RequestParam(required = false) String recordDate,
            @RequestParam(required = false) String planId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (page != null && size != null) {
            Map<String, Object> details = workoutRecordService.getWorkoutRecordDetailsPaged(userId, recordDate, planId, page, size);
            return Result.success("详情查询成功", details);
        }
        List<Map<String, Object>> details = workoutRecordService.getWorkoutRecordDetails(userId, recordDate, planId);
        return Result.success("详情查询成功", details);
    }

    // 训练部位统计
    @GetMapping("/muscle-stat")
    public Result<List<Map<String, Object>>> getMuscleStat(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Map<String, Object>> statData = workoutRecordService.getMuscleStat(userId);
        return Result.success("统计数据获取成功", statData);
    }
}
