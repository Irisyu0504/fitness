package org.example.fitness_backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.fitness_backend.entity.User;
import org.example.fitness_backend.service.AiChatService;
import org.example.fitness_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        SseEmitter emitter = new SseEmitter(120000L);

        // 会员检查
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null || user.getVipExpireTime() == null || user.getVipExpireTime().isBefore(LocalDateTime.now())) {
            try {
                emitter.send(SseEmitter.event()
                        .data("[ERROR] 会员已过期，请开通会员后使用 AI 助手", MediaType.TEXT_PLAIN));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> inputMessages = (List<Map<String, String>>) requestBody.get("messages");

        @SuppressWarnings("unchecked")
        Map<String, String> context = (Map<String, String>) requestBody.get("context");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", AiChatService.buildSystemPrompt(context)));
        if (inputMessages != null) {
            messages.addAll(inputMessages);
        }

        aiChatService.streamChat(messages)
                .subscribe(
                        content -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .data(content, MediaType.TEXT_PLAIN));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }
}
