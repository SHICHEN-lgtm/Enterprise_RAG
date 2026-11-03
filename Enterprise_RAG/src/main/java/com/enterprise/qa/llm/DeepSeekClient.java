package com.enterprise.qa.llm;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek 客户端（OpenAI 兼容 Chat Completions）。
 */
@Service
public class DeepSeekClient implements LlmClient {

    private final LlmProperties props;
    private final RestClient restClient;

    public DeepSeekClient(LlmProperties props) {
        this.props = props;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(props.getTimeoutSeconds()));
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .requestFactory(factory)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public String provider() {
        return props.getProvider();
    }

    @Override
    public LlmResult chat(String system, String user) {
        Map<String, Object> body = Map.of(
                "model", props.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                ),
                "temperature", props.getTemperature(),
                "max_tokens", props.getMaxTokens()
        );
        JsonNode resp = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + props.getApiKey())
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (resp == null) {
            return new LlmResult("", 0, 0);
        }
        String answer = resp.path("choices").path(0).path("message").path("content").asText("");
        int promptTokens = resp.path("usage").path("prompt_tokens").asInt(0);
        int completionTokens = resp.path("usage").path("completion_tokens").asInt(0);
        return new LlmResult(answer, promptTokens, completionTokens);
    }
}
