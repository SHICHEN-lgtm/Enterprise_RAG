package com.enterprise.qa.llm;

/**
 * 大模型客户端抽象，便于扩展多厂商（DeepSeek / GLM / 通义等）。
 */
public interface LlmClient {

    String provider();

    LlmResult chat(String system, String user);
}
