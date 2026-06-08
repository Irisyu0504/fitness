package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.DietRecord;
import org.example.fitness_backend.service.DietRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diet-records")
public class DietRecordController {

    @Autowired
    private DietRecordService dietRecordService;

    @PostMapping
    public Result<String> addDietRecord(@RequestBody DietRecord dietRecord, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        dietRecord.setUserId(userId);

        if (dietRecord.getFoodName() == null || dietRecord.getFoodName().trim().isEmpty()) {
            return Result.error(400, "食物名称不能为空");
        }
        if (dietRecord.getCalories() == null) {
            return Result.error(400, "食物热量不能为空");
        }

        boolean success = dietRecordService.save(dietRecord);
        return success ? Result.success("饮食记录添加成功", null) : Result.error(500, "记录失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateDietRecord(@PathVariable Long id, @RequestBody DietRecord dietRecord, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        UpdateWrapper<DietRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id).eq("user_id", userId);

        boolean success = dietRecordService.update(dietRecord, updateWrapper);
        return success ? Result.success("饮食记录修改成功", null) : Result.error(403, "修改失败，记录不存在或无权操作");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteDietRecord(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<DietRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("user_id", userId);

        boolean success = dietRecordService.remove(queryWrapper);
        return success ? Result.success("饮食记录删除成功", null) : Result.error(403, "删除失败，记录不存在或无权操作");
    }

    @GetMapping
    public Result<?> getDietRecordList(
            @RequestParam(required = false) String foodName,
            @RequestParam(required = false) String mealType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        QueryWrapper<DietRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (foodName != null && !foodName.trim().isEmpty()) {
            queryWrapper.like("food_name", foodName);
        }
        if (mealType != null && !mealType.trim().isEmpty()) {
            queryWrapper.eq("meal_type", mealType);
        }
        queryWrapper.orderByDesc("meal_time");

        if (page != null && size != null) {
            Page<DietRecord> pageResult = dietRecordService.page(new Page<>(page, size), queryWrapper);
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            return Result.success("查询成功", result);
        }
        return Result.success("查询成功", dietRecordService.list(queryWrapper));
    }

    @GetMapping("/calorie-stat")
    public Result<Map<String, Object>> getCalorieStat(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> statData = dietRecordService.getTodayCalorieStat(userId);
        return Result.success("统计数据获取成功", statData);
    }
}