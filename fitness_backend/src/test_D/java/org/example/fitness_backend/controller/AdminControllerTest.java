package org.example.fitness_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.common.Result;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.service.ExerciseService;
import org.example.fitness_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminControllerTest {

    @Test
    void getStatsRejectsNonAdmin() {
        UserService userService = mock(UserService.class);
        ExerciseService exerciseService = mock(ExerciseService.class);
        AdminController controller = controller(userService, exerciseService);

        when(userService.getById(2L)).thenReturn(userWithRole(2L, "user"));

        Result<Map<String, Object>> result = controller.getStats(requestForUser(2L));

        assertEquals(403, result.getCode());
        verify(userService, never()).count();
        verify(exerciseService, never()).count();
    }

    @Test
    void getStatsReturnsAggregatedCountsForAdmin() {
        UserService userService = mock(UserService.class);
        ExerciseService exerciseService = mock(ExerciseService.class);
        AdminController controller = controller(userService, exerciseService);

        when(userService.getById(1L)).thenReturn(userWithRole(1L, "admin"));
        when(userService.count()).thenReturn(12L);
        when(exerciseService.count()).thenReturn(34L);
        when(userService.count(any())).thenReturn(5L, 2L);

        Result<Map<String, Object>> result = controller.getStats(requestForUser(1L));

        assertEquals(200, result.getCode());
        assertEquals(12L, result.getData().get("totalUsers"));
        assertEquals(34L, result.getData().get("totalExercises"));
        assertEquals(5L, result.getData().get("vipUsers"));
        assertEquals(2L, result.getData().get("todayNewUsers"));
    }

    @Test
    void activateVipRejectsNonAdmin() {
        UserService userService = mock(UserService.class);
        ExerciseService exerciseService = mock(ExerciseService.class);
        AdminController controller = controller(userService, exerciseService);

        when(userService.getById(2L)).thenReturn(userWithRole(2L, "user"));

        Result<String> result = controller.activateVip(9L, daysBody(30), requestForUser(2L));

        assertEquals(403, result.getCode());
        verify(userService, never()).updateById(any());
    }

    @Test
    void activateVipRejectsNonPositiveDays() {
        UserService userService = mock(UserService.class);
        ExerciseService exerciseService = mock(ExerciseService.class);
        AdminController controller = controller(userService, exerciseService);

        when(userService.getById(1L)).thenReturn(userWithRole(1L, "admin"));

        Result<String> result = controller.activateVip(9L, daysBody(0), requestForUser(1L));

        assertEquals(400, result.getCode());
        verify(userService, never()).updateById(any());
    }

    @Test
    void activateVipStartsFromNowWhenUserHasNoActiveVip() {
        UserService userService = mock(UserService.class);
        ExerciseService exerciseService = mock(ExerciseService.class);
        AdminController controller = controller(userService, exerciseService);

        when(userService.getById(1L)).thenReturn(userWithRole(1L, "admin"));
        User targetUser = userWithRole(9L, "user");
        targetUser.setVipExpireTime(LocalDateTime.now().minusDays(2));
        when(userService.getById(9L)).thenReturn(targetUser);
        when(userService.updateById(targetUser)).thenReturn(true);

        Result<String> result = controller.activateVip(9L, daysBody(30), requestForUser(1L));

        assertEquals(200, result.getCode());
        assertNotNull(targetUser.getVipExpireTime());
        assertTrue(targetUser.getVipExpireTime().isAfter(LocalDateTime.now().plusDays(29)));
        verify(userService).updateById(targetUser);
    }

    @Test
    void activateVipExtendsExistingActiveVip() {
        UserService userService = mock(UserService.class);
        ExerciseService exerciseService = mock(ExerciseService.class);
        AdminController controller = controller(userService, exerciseService);

        when(userService.getById(1L)).thenReturn(userWithRole(1L, "admin"));
        User targetUser = userWithRole(9L, "user");
        LocalDateTime originalExpire = LocalDateTime.now().plusDays(10);
        targetUser.setVipExpireTime(originalExpire);
        when(userService.getById(9L)).thenReturn(targetUser);
        when(userService.updateById(targetUser)).thenReturn(true);

        Result<String> result = controller.activateVip(9L, daysBody(15), requestForUser(1L));

        assertEquals(200, result.getCode());
        assertTrue(targetUser.getVipExpireTime().isAfter(originalExpire.plusDays(14)));
        verify(userService).updateById(targetUser);
    }

    private AdminController controller(UserService userService, ExerciseService exerciseService) {
        AdminController controller = new AdminController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        ReflectionTestUtils.setField(controller, "exerciseService", exerciseService);
        return controller;
    }

    private HttpServletRequest requestForUser(Long userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(userId);
        return request;
    }

    private User userWithRole(Long id, String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private Map<String, Integer> daysBody(int days) {
        Map<String, Integer> body = new HashMap<>();
        body.put("days", days);
        return body;
    }
}
