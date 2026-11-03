package com.enterprise.qa.llm;

/**
 * LLM 调用结果：正文 + token 用量（用于效果评测）。
 */
public record LlmResult(String answer, int promptTokens, int completionTokens) {
}
