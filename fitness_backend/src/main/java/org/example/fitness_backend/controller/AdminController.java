package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.Exercise;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.service.ExerciseService;
import org.example.fitness_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ExerciseService exerciseService;

    private boolean isAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        return user != null && "admin".equals(user.getRole());
    }

    /**
     * 管理员数据概览
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "越权操作");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("totalExercises", exerciseService.count());

        // VIP 用户数
        QueryWrapper<User> vipQuery = new QueryWrapper<>();
        vipQuery.isNotNull("vip_expire_time").gt("vip_expire_time", LocalDateTime.now());
        stats.put("vipUsers", userService.count(vipQuery));

        // 今日新增用户
        QueryWrapper<User> todayQuery = new QueryWrapper<>();
        todayQuery.ge("create_time", LocalDate.now().atStartOfDay());
        stats.put("todayNewUsers", userService.count(todayQuery));

        return Result.success("查询成功", stats);
    }

    /**
     * 激活/延长会员
     */
    @PutMapping("/users/{id}/activate-vip")
    public Result<String> activateVip(@PathVariable Long id, @RequestBody Map<String, Integer> body, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "越权操作");

        Integer days = body.get("days");
        if (days == null || days <= 0) {
            return Result.error(400, "天数必须大于 0");
        }

        User targetUser = userService.getById(id);
        if (targetUser == null) return Result.error(404, "用户不存在");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpire = targetUser.getVipExpireTime();

        LocalDateTime newExpire;
        if (currentExpire != null && currentExpire.isAfter(now)) {
            newExpire = currentExpire.plusDays(days);
        } else {
            newExpire = now.plusDays(days);
        }

        targetUser.setVipExpireTime(newExpire);
        boolean success = userService.updateById(targetUser);
        return success ? Result.success("激活成功，到期：" + newExpire, null) : Result.error(500, "激活失败");
    }

    /**
     * 管理员查看用户详情
     */
    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) return Result.error(403, "越权操作");

        User user = userService.getById(id);
        if (user == null) return Result.error(404, "用户不存在");

        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("isVip", user.getVipExpireTime() != null && user.getVipExpireTime().isAfter(LocalDateTime.now()));
        result.put("expireTime", user.getVipExpireTime());

        return Result.success("查询成功", result);
    }
}
