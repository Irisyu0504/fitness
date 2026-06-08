package org.example.fitness_backend.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.FitnessGoal;
import org.example.fitness_backend.entity.WorkoutPlan;
import org.example.fitness_backend.service.FitnessGoalService;
import org.example.fitness_backend.service.WorkoutPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FitnessGoalControllerTest {

    @Test
    void updateRejectsTargetDateBeforeStartDate() {
        FitnessGoalService goalService = mock(FitnessGoalService.class);
        WorkoutPlanService workoutPlanService = mock(WorkoutPlanService.class);
        FitnessGoalController controller = controller(goalService, workoutPlanService);

        FitnessGoal requestBody = new FitnessGoal();
        requestBody.setStartDate(LocalDate.parse("2026-06-01"));
        requestBody.setTargetDate(LocalDate.parse("2026-05-01"));

        Result<String> result = controller.updateFitnessGoal(10L, requestBody, requestForUser(1L));

        assertEquals(400, result.getCode());
        verify(goalService, never()).updateById(any());
    }

    @Test
    void updateRequiresGoalOwnership() {
        FitnessGoalService goalService = mock(FitnessGoalService.class);
        WorkoutPlanService workoutPlanService = mock(WorkoutPlanService.class);
        FitnessGoalController controller = controller(goalService, workoutPlanService);

        FitnessGoal existing = new FitnessGoal();
        existing.setId(10L);
        existing.setUserId(2L);
        when(goalService.getById(10L)).thenReturn(existing);

        FitnessGoal requestBody = new FitnessGoal();
        requestBody.setStartDate(LocalDate.parse("2026-05-01"));
        requestBody.setTargetDate(LocalDate.parse("2026-06-01"));

        Result<String> result = controller.updateFitnessGoal(10L, requestBody, requestForUser(1L));

        assertEquals(404, result.getCode());
        verify(goalService, never()).updateById(any());
    }

    @Test
    void deleteRequiresOwnershipAndUnlinksWorkoutPlans() {
        FitnessGoalService goalService = mock(FitnessGoalService.class);
        WorkoutPlanService workoutPlanService = mock(WorkoutPlanService.class);
        FitnessGoalController controller = controller(goalService, workoutPlanService);

        FitnessGoal existing = new FitnessGoal();
        existing.setId(10L);
        existing.setUserId(1L);
        when(goalService.getById(10L)).thenReturn(existing);
        when(workoutPlanService.update(any(WorkoutPlan.class), any(Wrapper.class))).thenReturn(true);
        when(goalService.removeById(10L)).thenReturn(true);

        Result<String> result = controller.deleteFitnessGoal(10L, requestForUser(1L));

        assertEquals(200, result.getCode());
        verify(workoutPlanService).update(
                argThat(plan -> plan.getGoalId() == null),
                any(Wrapper.class)
        );
        verify(goalService).removeById(10L);
    }

    private FitnessGoalController controller(FitnessGoalService goalService, WorkoutPlanService workoutPlanService) {
        FitnessGoalController controller = new FitnessGoalController();
        ReflectionTestUtils.setField(controller, "fitnessGoalService", goalService);
        ReflectionTestUtils.setField(controller, "workoutPlanService", workoutPlanService);
        return controller;
    }

    private HttpServletRequest requestForUser(Long userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(userId);
        return request;
    }
}
