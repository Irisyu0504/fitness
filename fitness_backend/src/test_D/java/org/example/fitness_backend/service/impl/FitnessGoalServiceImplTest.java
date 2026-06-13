package org.example.fitness_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.example.fitness_backend.entity.BodyRecord;
import org.example.fitness_backend.entity.FitnessGoal;
import org.example.fitness_backend.mapper.BodyRecordMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FitnessGoalServiceImplTest {

    @Test
    void calculateGoalProgressReturnsNoGoalWhenCurrentGoalIsMissing() {
        TestableFitnessGoalServiceImpl service = serviceWith(null, null);

        Map<String, Object> result = service.calculateGoalProgress(1L);

        assertFalse((Boolean) result.get("hasGoal"));
        assertEquals("暂无进行中的健身目标", result.get("msg"));
    }

    @Test
    void calculateGoalProgressReturnsZeroWhenWeightGoalHasSameStartAndTargetWeight() {
        FitnessGoal goal = weightGoal("减脂", "70", "70");
        TestableFitnessGoalServiceImpl service = serviceWith(goal, null);

        Map<String, Object> result = service.calculateGoalProgress(1L);

        assertTrue((Boolean) result.get("hasGoal"));
        assertEquals(0, result.get("progressRate"));
        assertEquals(new BigDecimal("70"), result.get("latestWeight"));
    }

    @Test
    void calculateGoalProgressClampsCompletedFatLossToHundred() {
        FitnessGoal goal = weightGoal("减脂", "80", "70");
        BodyRecord latestRecord = bodyRecord("68");
        TestableFitnessGoalServiceImpl service = serviceWith(goal, latestRecord);

        Map<String, Object> result = service.calculateGoalProgress(1L);

        assertEquals(new BigDecimal("68"), result.get("latestWeight"));
        assertEquals(100L, result.get("progressRate"));
    }

    @Test
    void calculateGoalProgressCalculatesPartialMuscleGain() {
        FitnessGoal goal = weightGoal("增肌", "60", "70");
        BodyRecord latestRecord = bodyRecord("65");
        TestableFitnessGoalServiceImpl service = serviceWith(goal, latestRecord);

        Map<String, Object> result = service.calculateGoalProgress(1L);

        assertEquals(50L, result.get("progressRate"));
    }

    @Test
    void calculateGoalProgressClampsExpiredTimeBasedGoalToHundred() {
        FitnessGoal goal = new FitnessGoal();
        goal.setGoalType("维持体重");
        goal.setCurrentWeight(new BigDecimal("65"));
        goal.setTargetWeight(new BigDecimal("65"));
        goal.setStartDate(LocalDate.now().minusDays(20));
        goal.setTargetDate(LocalDate.now().minusDays(5));

        TestableFitnessGoalServiceImpl service = serviceWith(goal, null);

        Map<String, Object> result = service.calculateGoalProgress(1L);

        assertEquals(100L, result.get("progressRate"));
    }

    private TestableFitnessGoalServiceImpl serviceWith(FitnessGoal goal, BodyRecord latestRecord) {
        BodyRecordMapper bodyRecordMapper = mock(BodyRecordMapper.class);
        when(bodyRecordMapper.selectOne(anyBodyRecordQuery())).thenReturn(latestRecord);

        TestableFitnessGoalServiceImpl service = new TestableFitnessGoalServiceImpl(goal);
        ReflectionTestUtils.setField(service, "bodyRecordMapper", bodyRecordMapper);
        return service;
    }

    private FitnessGoal weightGoal(String goalType, String currentWeight, String targetWeight) {
        FitnessGoal goal = new FitnessGoal();
        goal.setGoalType(goalType);
        goal.setCurrentWeight(new BigDecimal(currentWeight));
        goal.setTargetWeight(new BigDecimal(targetWeight));
        return goal;
    }

    private BodyRecord bodyRecord(String weight) {
        BodyRecord record = new BodyRecord();
        record.setWeight(new BigDecimal(weight));
        return record;
    }

    private QueryWrapper<BodyRecord> anyBodyRecordQuery() {
        return ArgumentMatchers.<QueryWrapper<BodyRecord>>any();
    }

    private static class TestableFitnessGoalServiceImpl extends FitnessGoalServiceImpl {
        private final FitnessGoal goal;

        private TestableFitnessGoalServiceImpl(FitnessGoal goal) {
            this.goal = goal;
        }

        @Override
        public FitnessGoal getOne(Wrapper<FitnessGoal> queryWrapper) {
            return goal;
        }
    }
}
