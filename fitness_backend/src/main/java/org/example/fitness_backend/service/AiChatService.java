package org.example.fitness_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class AiChatService {

    private final WebClient webClient;
    private final String model;

    public AiChatService(
            @Value("${ai.deepseek.api-key}") String apiKey,
            @Value("${ai.deepseek.base-url}") String baseUrl,
            @Value("${ai.deepseek.model}") String model
    ) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * 流式调用 DeepSeek API，返回每个 token 的内容
     */
    public Flux<String> streamChat(List<Map<String, String>> messages) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", messages,
                "stream", true
        );

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> !line.isBlank() && !line.equals("[DONE]"))
                .map(this::extractContent)
                .filter(content -> content != null && !content.isEmpty());
    }

    /**
     * 从 SSE data 行中提取 content 字段
     * DeepSeek 返回格式: data: {"choices":[{"delta":{"content":"xxx"}}]}
     */
    private String extractContent(String sseLine) {
        try {
            String json = sseLine.startsWith("data: ") ? sseLine.substring(6) : sseLine;
            if (json.isBlank() || json.equals("[DONE]")) return null;

            // 简单字符串解析，避免引入额外 JSON 库
            // 查找 "delta":{"content":"..."} 模式
            int deltaIdx = json.indexOf("\"delta\"");
            if (deltaIdx == -1) return null;

            int contentIdx = json.indexOf("\"content\":", deltaIdx);
            if (contentIdx == -1) return null;

            int startQuote = json.indexOf("\"", contentIdx + 10);
            if (startQuote == -1) return null;

            // 找到值的结束引号（处理转义引号）
            StringBuilder sb = new StringBuilder();
            int i = startQuote + 1;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    if (next == '"') { sb.append('"'); i += 2; continue; }
                    if (next == 'n') { sb.append('\n'); i += 2; continue; }
                    if (next == 't') { sb.append('\t'); i += 2; continue; }
                    if (next == '\\') { sb.append('\\'); i += 2; continue; }
                }
                if (c == '"') break;
                sb.append(c);
                i++;
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构造健身教练 system prompt
     */
    public static String buildSystemPrompt(Map<String, String> context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是 CoreFitness 智能健身教练，一位专业、友好、鼓励型的 AI 健身顾问。\n\n");
        sb.append("## 你的能力\n");
        sb.append("- 根据用户身体数据和目标，制定个性化训练方案\n");
        sb.append("- 解答动作要领、呼吸技巧、常见错误纠正\n");
        sb.append("- 提供饮食营养建议（热量、蛋白质、碳水搭配）\n");
        sb.append("- 分析训练数据，追踪进度，调整计划\n");
        sb.append("- 解答运动损伤预防与恢复知识\n\n");
        sb.append("## 回答规范\n");
        sb.append("- 用中文回答，语气亲切专业\n");
        sb.append("- 回答简洁实用，避免冗长说教\n");
        sb.append("- 涉及训练计划时给出具体动作、组数、次数、休息时间\n");
        sb.append("- 涉及饮食时给出大致热量和营养素参考\n");
        sb.append("- 如果用户数据不足以给出精确建议，先询问关键信息\n");
        sb.append("- 禁止使用任何 emoji 表情符号\n");
        sb.append("- 禁止使用 Markdown 标题（# ## ### 等），不要用 # 开头的行\n");
        sb.append("- 每段回答结束后换行，段落之间用空行分隔\n");

        if (context != null && !context.isEmpty()) {
            sb.append("\n## 用户信息\n");
            context.forEach((k, v) -> {
                if (v != null && !v.isEmpty()) {
                    sb.append("- ").append(k).append("：").append(v).append("\n");
                }
            });
        }

        return sb.toString();
    }
}
