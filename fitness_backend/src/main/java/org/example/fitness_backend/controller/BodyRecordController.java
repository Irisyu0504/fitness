package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.service.BodyRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/body-records")
public class BodyRecordController {
    @Autowired
    private BodyRecordService bodyRecordService;

    @PostMapping
    public Result<String> addRecord(@RequestBody BodyRecord bodyRecord, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        bodyRecord.setUserId(userId);

        if(bodyRecord.getWeight() == null || bodyRecord.getRecordDate() == null) {
            return Result.error(400, "体重和记录时期不能为空");
        }

        boolean success = bodyRecordService.save(bodyRecord);
        return success ? Result.success("身体数据记录成功", null) : Result.error(500, "身体数据记录失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateRecord(@PathVariable Long id, @RequestBody BodyRecord bodyRecord, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        bodyRecord.setId(id);
        bodyRecord.setUserId(userId);

        boolean success = bodyRecordService.updateById(bodyRecord);
        return success ? Result.success("身体数据修改成功", null) : Result.error(500, "身体数据修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteRecord(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        QueryWrapper<BodyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id).eq("user_id", userId);

        boolean success = bodyRecordService.remove(queryWrapper);
        return success ? Result.success("身体数据删除成功", null) : Result.error(403, "删除失败，记录不存在或无权操作");
    }

    @GetMapping
    public Result<?> getRecordList(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        QueryWrapper<BodyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("record_date");

        if (page != null && size != null) {
            Page<BodyRecord> pageResult = bodyRecordService.page(new Page<>(page, size), queryWrapper);
            pageResult.getRecords().forEach(bodyRecordService::calculateAndSetBmi);
            Map<String, Object> result = new HashMap<>();
            result.put("records", pageResult.getRecords());
            result.put("total", pageResult.getTotal());
            return Result.success("查询成功", result);
        }
        List<BodyRecord> list = bodyRecordService.list(queryWrapper);
        list.forEach(bodyRecordService::calculateAndSetBmi);
        return Result.success("查询成功", list);
    }

    @GetMapping("/trend")
    public Result<List<BodyRecord>> getTrendData(@RequestParam(defaultValue = "30") Integer days, HttpServletRequest request){
        Long userId = (Long) request.getAttribute("userId");

        LocalDate startDate = LocalDate.now().minusDays(days);

        QueryWrapper<BodyRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).ge("record_date", startDate).orderByAsc("record_date");

        List<BodyRecord> list = bodyRecordService.list(queryWrapper);
        list.forEach(bodyRecordService::calculateAndSetBmi);
        return Result.success("查询趋势数据成功", list);
    }
}
