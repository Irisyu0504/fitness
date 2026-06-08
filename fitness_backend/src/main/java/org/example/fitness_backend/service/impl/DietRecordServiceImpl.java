package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.fitness_backend.entity.DietRecord;
import org.example.fitness_backend.mapper.DietRecordMapper;
import org.example.fitness_backend.service.DietRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DietRecordServiceImpl extends ServiceImpl<DietRecordMapper, DietRecord> implements DietRecordService {
    private static final BigDecimal PROTEIN_CALORIES_PER_GRAM = BigDecimal.valueOf(4);
    private static final BigDecimal CARBS_CALORIES_PER_GRAM = BigDecimal.valueOf(4);
    private static final BigDecimal FAT_CALORIES_PER_GRAM = BigDecimal.valueOf(9);

    private static final int PROTEIN_MIN_PERCENT = 10;
    private static final int PROTEIN_MAX_PERCENT = 35;
    private static final int CARBS_MIN_PERCENT = 45;
    private static final int CARBS_MAX_PERCENT = 65;
    private static final int FAT_MIN_PERCENT = 20;
    private static final int FAT_MAX_PERCENT = 35;

    @Override
    public boolean save(DietRecord entity) {
        if (entity.getMealTime() == null) {
            entity.setMealTime(LocalDateTime.now());
        }
        entity.setMealTime(normalizeMealTimeToMinute(entity.getMealTime()));
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        return super.save(entity);
    }

    @Override
    public boolean updateById(DietRecord entity) {
        if (entity.getMealTime() != null) {
            entity.setMealTime(normalizeMealTimeToMinute(entity.getMealTime()));
        }
        entity.setUpdateTime(LocalDateTime.now());
        return super.updateById(entity);
    }

    @Override
    public boolean update(DietRecord entity, Wrapper<DietRecord> updateWrapper) {
        if (entity != null) {
            if (entity.getMealTime() != null) {
                entity.setMealTime(normalizeMealTimeToMinute(entity.getMealTime()));
            }
            entity.setUpdateTime(LocalDateTime.now());
        }
        return super.update(entity, updateWrapper);
    }

    // 统计今日热量与三大营养素摄入
    @Override
    public Map<String, Object> getTodayCalorieStat(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime startOfNextDay = today.plusDays(1).atStartOfDay();

        QueryWrapper<DietRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .ge("meal_time", startOfDay)
                .lt("meal_time", startOfNextDay);

        List<DietRecord> todayRecords = this.list(queryWrapper);

        BigDecimal totalCalories = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarbs = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;

        for (DietRecord record : todayRecords) {
            if (record.getCalories() != null) totalCalories = totalCalories.add(record.getCalories());
            if (record.getProtein() != null) totalProtein = totalProtein.add(record.getProtein());
            if (record.getCarbs() != null) totalCarbs = totalCarbs.add(record.getCarbs());
            if (record.getFat() != null) totalFat = totalFat.add(record.getFat());
        }

        Map<String, Object> statResult = new HashMap<>();
        statResult.put("totalCalories", totalCalories);
        statResult.put("totalProtein", totalProtein);
        statResult.put("totalCarbs", totalCarbs);
        statResult.put("totalFat", totalFat);
        statResult.put("recordCount", todayRecords.size());
        statResult.putAll(buildMacroAnalysis(totalProtein, totalCarbs, totalFat, todayRecords.size()));

        return statResult;
    }

    Map<String, Object> buildMacroAnalysis(BigDecimal totalProtein, BigDecimal totalCarbs, BigDecimal totalFat, int recordCount) {
        BigDecimal proteinCalories = toZero(totalProtein).multiply(PROTEIN_CALORIES_PER_GRAM);
        BigDecimal carbsCalories = toZero(totalCarbs).multiply(CARBS_CALORIES_PER_GRAM);
        BigDecimal fatCalories = toZero(totalFat).multiply(FAT_CALORIES_PER_GRAM);
        BigDecimal totalMacroCalories = proteinCalories.add(carbsCalories).add(fatCalories);

        int proteinPercent = calculatePercent(proteinCalories, totalMacroCalories);
        int carbsPercent = calculatePercent(carbsCalories, totalMacroCalories);
        int fatPercent = calculatePercent(fatCalories, totalMacroCalories);

        Map<String, Object> macroPercentages = new HashMap<>();
        macroPercentages.put("proteinPercent", proteinPercent);
        macroPercentages.put("carbsPercent", carbsPercent);
        macroPercentages.put("fatPercent", fatPercent);

        Map<String, Object> result = new HashMap<>();
        result.put("proteinPercent", proteinPercent);
        result.put("carbsPercent", carbsPercent);
        result.put("fatPercent", fatPercent);
        result.put("macroPercentages", macroPercentages);
        result.put("macroReminder", buildMacroReminder(proteinPercent, carbsPercent, fatPercent, recordCount, totalMacroCalories));
        return result;
    }

    LocalDateTime normalizeMealTimeToMinute(LocalDateTime mealTime) {
        return mealTime == null ? null : mealTime.truncatedTo(ChronoUnit.MINUTES);
    }

    private BigDecimal toZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int calculatePercent(BigDecimal value, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String buildMacroReminder(int proteinPercent, int carbsPercent, int fatPercent, int recordCount, BigDecimal totalMacroCalories) {
        if (recordCount <= 0) {
            return "今天还未记录，快来添加饮食记录吧";
        }
        if (totalMacroCalories.compareTo(BigDecimal.ZERO) <= 0) {
            return "今天的三大营养素数据还不完整，补充蛋白质、脂肪和碳水后再看占比。";
        }

        if (carbsPercent > CARBS_MAX_PERCENT) {
            return "碳水占比偏高（" + carbsPercent + "%），高于推荐上限 " + CARBS_MAX_PERCENT + "%，建议减少精制主食和含糖食物。";
        }
        if (fatPercent > FAT_MAX_PERCENT) {
            return "脂肪占比偏高（" + fatPercent + "%），高于推荐上限 " + FAT_MAX_PERCENT + "%，建议减少油炸和高脂食物。";
        }
        if (proteinPercent > PROTEIN_MAX_PERCENT) {
            return "蛋白质占比偏高（" + proteinPercent + "%），高于推荐上限 " + PROTEIN_MAX_PERCENT + "%，建议搭配足量蔬菜和主食。";
        }
        if (carbsPercent < CARBS_MIN_PERCENT) {
            return "碳水占比偏低（" + carbsPercent + "%），低于推荐下限 " + CARBS_MIN_PERCENT + "%，可适量补充全谷物或薯类。";
        }
        if (fatPercent < FAT_MIN_PERCENT) {
            return "脂肪占比偏低（" + fatPercent + "%），低于推荐下限 " + FAT_MIN_PERCENT + "%，可适量增加坚果或植物油。";
        }
        if (proteinPercent < PROTEIN_MIN_PERCENT) {
            return "蛋白质占比偏低（" + proteinPercent + "%），低于推荐下限 " + PROTEIN_MIN_PERCENT + "%，可增加鸡蛋、鱼肉或豆制品。";
        }

        return "今日三大营养素占比在推荐范围内，继续保持。";
    }
}
