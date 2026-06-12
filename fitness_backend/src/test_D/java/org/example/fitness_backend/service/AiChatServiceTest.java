package org.example.fitness_backend.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiChatServiceTest {

    private final AiChatService service = new AiChatService("test-key", "http://localhost", "test-model");

    @Test
    void buildSystemPromptIncludesContextWhenProvided() {
        String prompt = AiChatService.buildSystemPrompt(Map.of(
                "目标", "减脂",
                "训练频率", "每周4次"
        ));

        assertTrue(prompt.contains("CoreFitness"));
        assertTrue(prompt.contains("目标"));
        assertTrue(prompt.contains("减脂"));
        assertTrue(prompt.contains("训练频率"));
    }

    @Test
    void buildSystemPromptOmitsUserSectionWhenContextIsEmpty() {
        String prompt = AiChatService.buildSystemPrompt(Map.of());

        assertTrue(prompt.contains("CoreFitness"));
        assertTrue(!prompt.contains("用户信息"));
    }

    @Test
    void extractContentReadsSseDataPrefix() {
        String content = ReflectionTestUtils.invokeMethod(
                service,
                "extractContent",
                "data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}"
        );

        assertEquals("你好", content);
    }

    @Test
    void extractContentDecodesEscapedCharacters() {
        String content = ReflectionTestUtils.invokeMethod(
                service,
                "extractContent",
                "data: {\"choices\":[{\"delta\":{\"content\":\"a\\\\b\\n\\t\\\"c\"}}]}"
        );

        assertEquals("a\\b\n\t\"c", content);
    }

    @Test
    void extractContentReturnsNullForDoneMarker() {
        String content = ReflectionTestUtils.invokeMethod(service, "extractContent", "[DONE]");

        assertNull(content);
    }

    @Test
    void extractContentReturnsNullWhenDeltaContentIsMissing() {
        String content = ReflectionTestUtils.invokeMethod(
                service,
                "extractContent",
                "data: {\"choices\":[{\"message\":{\"content\":\"你好\"}}]}"
        );

        assertNull(content);
    }
}
