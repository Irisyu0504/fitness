package org.example.fitness_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.service.AiChatService;
import org.example.fitness_backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiChatControllerTest {

    @Test
    void chatRejectsMissingVipUserBeforeCallingAiService() {
        AiChatService aiChatService = mock(AiChatService.class);
        UserService userService = mock(UserService.class);
        AiChatController controller = controller(aiChatService, userService);

        when(userService.getById(1L)).thenReturn(userWithVip(1L, null));

        SseEmitter emitter = controller.chat(Map.of(), requestForUser(1L));

        assertNotNull(emitter);
        verify(aiChatService, never()).streamChat(any());
    }

    @Test
    void chatRejectsExpiredVipUserBeforeCallingAiService() {
        AiChatService aiChatService = mock(AiChatService.class);
        UserService userService = mock(UserService.class);
        AiChatController controller = controller(aiChatService, userService);

        when(userService.getById(1L)).thenReturn(userWithVip(1L, LocalDateTime.now().minusMinutes(1)));

        SseEmitter emitter = controller.chat(Map.of(), requestForUser(1L));

        assertNotNull(emitter);
        verify(aiChatService, never()).streamChat(any());
    }

    @Test
    void chatPrependsSystemPromptAndForwardsUserMessagesForVipUser() {
        AiChatService aiChatService = mock(AiChatService.class);
        UserService userService = mock(UserService.class);
        AiChatController controller = controller(aiChatService, userService);

        when(userService.getById(1L)).thenReturn(userWithVip(1L, LocalDateTime.now().plusDays(30)));
        when(aiChatService.streamChat(any())).thenReturn(Flux.empty());

        List<Map<String, String>> inputMessages = List.of(
                Map.of("role", "user", "content", "我今天适合练什么")
        );
        Map<String, String> context = Map.of(
                "目标", "减脂",
                "训练经验", "3个月"
        );

        SseEmitter emitter = controller.chat(
                Map.of("messages", inputMessages, "context", context),
                requestForUser(1L)
        );

        assertNotNull(emitter);
        verify(aiChatService).streamChat(argThat(messages ->
                messages.size() == 2
                        && "system".equals(messages.get(0).get("role"))
                        && messages.get(0).get("content").contains("CoreFitness")
                        && messages.get(0).get("content").contains("目标")
                        && messages.get(1).equals(inputMessages.get(0))
        ));
    }

    @Test
    void chatStillCallsAiServiceWhenClientSendsNoMessages() {
        AiChatService aiChatService = mock(AiChatService.class);
        UserService userService = mock(UserService.class);
        AiChatController controller = controller(aiChatService, userService);

        when(userService.getById(1L)).thenReturn(userWithVip(1L, LocalDateTime.now().plusDays(30)));
        when(aiChatService.streamChat(any())).thenReturn(Flux.empty());

        SseEmitter emitter = controller.chat(
                Map.of("context", Map.of("目标", "增肌")),
                requestForUser(1L)
        );

        assertNotNull(emitter);
        verify(aiChatService).streamChat(argThat(messages ->
                messages.size() == 1
                        && "system".equals(messages.get(0).get("role"))
                        && messages.get(0).get("content").contains("增肌")
        ));
    }

    private AiChatController controller(AiChatService aiChatService, UserService userService) {
        AiChatController controller = new AiChatController();
        ReflectionTestUtils.setField(controller, "aiChatService", aiChatService);
        ReflectionTestUtils.setField(controller, "userService", userService);
        return controller;
    }

    private HttpServletRequest requestForUser(Long userId) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(userId);
        return request;
    }

    private User userWithVip(Long id, LocalDateTime vipExpireTime) {
        User user = new User();
        user.setId(id);
        user.setVipExpireTime(vipExpireTime);
        return user;
    }
}
