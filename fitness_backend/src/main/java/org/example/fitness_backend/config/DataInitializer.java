package org.example.fitness_backend.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.fitness_backend.entity.*;
import org.example.fitness_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 应用启动时的数据初始化器。
 * <p>实现 {@code CommandLineRunner}，在 Spring 容器就绪后自动执行：</p>
 * <ol>
 *   <li>确保 users 表包含 vip_expire_time 列（兼容旧数据库）</li>
 *   <li>创建默认管理员账号和测试用户</li>
 *   <li>为测试用户填充示例数据（体测记录、健身目标、训练记录、饮食记录）</li>
 * </ol>
 * <p>所有密码均通过 BCrypt 加密存储，使用 INSERT IGNORE 保证幂等性。</p>
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserService userService;
    @Autowired private BodyRecordService bodyRecordService;
    @Autowired private FitnessGoalService fitnessGoalService;
    @Autowired private WorkoutPlanService workoutPlanService;
    @Autowired private WorkoutRecordService workoutRecordService;
    @Autowired private DietRecordService dietRecordService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        ensureVipColumn();
        User admin = ensureUser("admin", "yxrs0504", "管理员", "admin", 175.0);

        // VIP 会员用户（到期时间在未来）
        User testUser = ensureUser("testuser", "Test@2026!", "健身达人", "user", 178.0);
        if (testUser != null) {
            setVip(testUser, 30); // 30 天会员
            seedTestData(testUser.getId());
        }

        User iris = ensureUser("Iris", "yxrs0504", "Iris", "user", 165.0);
        if (iris != null) {
            setVip(iris, 90); // 90 天会员
            seedTestData(iris.getId());
        }

        User vipDemo = ensureUser("vipdemo", "Vip@2026!", "VIP体验用户", "user", 170.0);
        if (vipDemo != null) {
            setVip(vipDemo, 365); // 年卡会员
        }

        // 普通用户（无会员）
        ensureUser("zhangsan", "Zhang@2026!", "张三", "user", 172.0);
        ensureUser("lisi", "Li@2026!", "李四", "user", 168.0);

        // 会员已过期的用户
        User expired = ensureUser("expired", "Exp@2026!", "过期会员", "user", 175.0);
        if (expired != null) {
            setVip(expired, -10); // 10 天前过期
        }
    }

    /**
     * 确保 users 表包含 vip_expire_time 字段。
     * 首次运行时执行 ALTER TABLE 添加列，后续运行时列已存在会抛出异常，安全忽略即可。
     */
    private void ensureVipColumn() {
        try {
            jdbcTemplate.execute("ALTER TABLE `users` ADD COLUMN `vip_expire_time` DATETIME DEFAULT NULL");
        } catch (Exception e) {
            // 列已存在时 MySQL 会抛出 DuplicateColumnException，属于正常情况，无需处理
        }
    }

    private void setVip(User user, int days) {
        LocalDateTime expire = LocalDateTime.now().plusDays(days);
        user.setVipExpireTime(expire);
        userService.updateById(user);
    }

    private User ensureUser(String username, String rawPassword, String nickname, String role, Double height) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("username", username);
        if (userService.count(qw) > 0) return userService.getOne(qw);

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(nickname);
        user.setRole(role);
        user.setHeight(new BigDecimal(height.toString()));
        user.setStatus(true);
        userService.save(user);
        return user;
    }

    private void seedTestData(Long userId) {
        QueryWrapper<BodyRecord> check = new QueryWrapper<>();
        check.eq("user_id", userId);
        if (bodyRecordService.count(check) > 0) return;

        bodyRecordService.save(makeBody(userId, 82.5, 22.0, 86.0, 26.1, "2026-04-28"));
        bodyRecordService.save(makeBody(userId, 82.0, 21.8, 85.5, 25.9, "2026-05-01"));
        bodyRecordService.save(makeBody(userId, 81.6, 21.5, 85.0, 25.8, "2026-05-05"));
        bodyRecordService.save(makeBody(userId, 81.2, 21.2, 84.5, 25.7, "2026-05-10"));
        bodyRecordService.save(makeBody(userId, 80.8, 20.9, 84.0, 25.5, "2026-05-15"));
        bodyRecordService.save(makeBody(userId, 80.3, 20.5, 83.5, 25.4, "2026-05-20"));
        bodyRecordService.save(makeBody(userId, 79.8, 20.1, 83.0, 25.2, "2026-05-27"));

        FitnessGoal goal = new FitnessGoal();
        goal.setUserId(userId);
        goal.setGoalType("减脂");
        goal.setCurrentWeight(new BigDecimal("82.5"));
        goal.setTargetWeight(new BigDecimal("75.0"));
        goal.setStartDate(LocalDate.parse("2026-04-28"));
        goal.setTargetDate(LocalDate.parse("2026-08-28"));
        goal.setWeeklyTarget(new BigDecimal("0.5"));
        goal.setStatus("进行中");
        fitnessGoalService.save(goal);

        WorkoutPlan plan = new WorkoutPlan();
        plan.setUserId(userId);
        plan.setGoalId(goal.getId());
        plan.setPlanName("减脂塑形计划");
        plan.setPlanGoal("减少体脂率至18%");
        plan.setStartDate(LocalDate.parse("2026-05-01"));
        plan.setEndDate(LocalDate.parse("2026-08-01"));
        plan.setFrequency("每周5次");
        plan.setDescription("结合力量训练与有氧运动，控制饮食热量");
        plan.setStatus("进行中");
        workoutPlanService.save(plan);

        saveWorkout(userId, plan.getId(), 20001L, 30, 4, 12, 240.0, "2026-05-26");
        saveWorkout(userId, plan.getId(), 20003L, 15, 3, 20, 105.0, "2026-05-26");
        saveWorkout(userId, plan.getId(), 20017L, 40, null, null, 440.0, "2026-05-27");
        saveWorkout(userId, plan.getId(), 20014L, 10, 3, null, 40.0, "2026-05-27");
        saveWorkout(userId, plan.getId(), 20007L, 25, 5, 10, 250.0, "2026-05-25");

        saveDiet(userId, "全麦面包+鸡蛋+牛奶", "早餐", 420, 25, 45, 15, "2026-05-27T08:00:00");
        saveDiet(userId, "鸡胸肉沙拉+糙米饭", "午餐", 550, 40, 55, 12, "2026-05-27T12:00:00");
        saveDiet(userId, "香蕉+坚果", "加餐", 180, 4, 28, 8, "2026-05-27T15:30:00");
        saveDiet(userId, "三文鱼+西兰花+红薯", "晚餐", 480, 35, 40, 18, "2026-05-27T18:30:00");
    }

    private BodyRecord makeBody(Long userId, double weight, double fat, double waist, double bmi, String date) {
        BodyRecord r = new BodyRecord();
        r.setUserId(userId);
        r.setWeight(new BigDecimal(weight));
        r.setBodyFatRate(new BigDecimal(fat));
        r.setWaistline(new BigDecimal(waist));
        r.setBmi(new BigDecimal(bmi));
        r.setRecordDate(LocalDate.parse(date));
        return r;
    }

    private void saveWorkout(Long userId, Long planId, Long exerciseId, int duration, Integer sets, Integer reps, double cal, String date) {
        WorkoutRecord wr = new WorkoutRecord();
        wr.setUserId(userId);
        wr.setPlanId(planId);
        wr.setExerciseId(exerciseId);
        wr.setDuration(duration);
        wr.setSetsCount(sets);
        wr.setReps(reps);
        wr.setCaloriesBurned(new BigDecimal(cal));
        wr.setRecordDate(LocalDate.parse(date));
        workoutRecordService.save(wr);
    }

    private void saveDiet(Long userId, String food, String type, double cal, double protein, double carbs, double fat, String time) {
        DietRecord dr = new DietRecord();
        dr.setUserId(userId);
        dr.setFoodName(food);
        dr.setMealType(type);
        dr.setCalories(new BigDecimal(cal));
        dr.setProtein(new BigDecimal(protein));
        dr.setCarbs(new BigDecimal(carbs));
        dr.setFat(new BigDecimal(fat));
        dr.setMealTime(LocalDateTime.parse(time));
        dietRecordService.save(dr);
    }
}
