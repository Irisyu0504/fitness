package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.service.UserService;
import org.example.fitness_backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user){
        if(user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }
        if(user.getPassword() == null || user.getPassword().length() < 6) {
            return Result.error(400, "密码不能为空，且不少于6位");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        long count = userService.count(queryWrapper);
        if(count > 0) {
            return Result.error(400, "用户名已存在，请更换！");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        boolean success = userService.save(user);
        return success ? Result.success("注册成功", null) : Result.error(500, "注册失败");
    }


    @PostMapping("/login")
    public Result<String> login(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null) {
            return Result.error(400, "用户名或密码不能为空！");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User dbuser = userService.getOne(queryWrapper);

        if(dbuser == null || !passwordEncoder.matches(user.getPassword(), dbuser.getPassword())) {
            return Result.error(400, "用户名或密码错误！");
        }

        String realToken = jwtUtils.generateToken(dbuser.getId());

        return Result.success("登录成功", realToken);
    }

    @GetMapping("/profile")
    public Result<User> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        User user = userService.getById(userId);

        if(user != null) {
            user.setPassword(null);
        }
        return Result.success("查询成功", user);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody User user, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");

        user.setId(userId);

        user.setUsername(null);
        user.setPassword(null);

        boolean success = userService.updateById(user);
        return success ? Result.success("个人信息修改成功", null) : Result.error(500, "个人信息修改失败");
    }

    @GetMapping("/vip-status")
    public Result<Map<String, Object>> getVipStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);

        Map<String, Object> result = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = user != null ? user.getVipExpireTime() : null;

        boolean isVip = expireTime != null && expireTime.isAfter(now);
        result.put("isVip", isVip);
        result.put("expireTime", expireTime);

        if (isVip) {
            long days = ChronoUnit.DAYS.between(now, expireTime);
            result.put("remainingDays", days);
        } else {
            result.put("remainingDays", 0);
        }

        return Result.success("查询成功", result);
    }

    @GetMapping("/admin/users")
    public Result<Object> getAllUsers(@RequestParam(required = false) String username, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        User admin = userService.getById(adminId);

        // 权限校验：必须是管理员
        if (admin == null || !"admin".equals(admin.getRole())) {
            return Result.error(403, "越权操作：只有管理员可查看全站用户");
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username);
        }
        queryWrapper.orderByDesc("create_time");

        // 查出列表后，把所有人的密码抹除再返回
        List<User> userList = userService.list(queryWrapper);
        userList.forEach(u -> u.setPassword(null));

        return Result.success("查询成功", userList);
    }


    @PutMapping("/admin/users/{id}/status")
    public Result<String> updateUserStatus(@PathVariable Long id, @RequestBody User userParam, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        User admin = userService.getById(adminId);

        if (admin == null || !"admin".equals(admin.getRole())) {
            return Result.error(403, "越权操作：只有管理员可修改用户状态");
        }

        if (id.equals(adminId)) {
            return Result.error(400, "管理员不能封禁自己");
        }

        User targetUser = new User();
        targetUser.setId(id);
        targetUser.setStatus(userParam.getStatus()); // 1正常，0禁用

        boolean success = userService.updateById(targetUser);
        return success ? Result.success("状态修改成功", null) : Result.error(500, "状态修改失败");
    }
}
