package com.enterprise.qa.dto;

import java.util.List;
import java.util.Map;

/**
 * 问答响应：答案 + 命中知识来源 + 效果指标（延迟、token）。
 */
public record ChatResponse(
        String question,
        String answer,
        List<Map<String, Object>> sources,
        Long latencyMs,
        String model,
        Integer promptTokens,
        Integer completionTokens
) {
}
