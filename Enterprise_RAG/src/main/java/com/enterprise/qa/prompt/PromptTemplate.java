package com.enterprise.qa.prompt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Prompt 规则沉淀：系统提示词与用户模板集中在此，可随 application.yml 的 prompt 节点调整。
 * 问答链路统一经此处组装，便于评测不同 Prompt 版本对回答质量的影响。
 */
@Component
public class PromptTemplate {

    @Value("${prompt.system}")
    private String systemTemplate;

    @Value("${prompt.user}")
    private String userTemplate;

    /** 组装 system 提示词 */
    public String buildSystem() {
        return systemTemplate == null ? defaultSystem() : systemTemplate;
    }

    /** 组装 user 消息：将检索到的知识片段与用户问题填入模板 */
    public String buildUser(String context, String question) {
        String tpl = (userTemplate == null || userTemplate.isBlank()) ? defaultUser() : userTemplate;
        return tpl.replace("{context}", context).replace("{question}", question);
    }

    private String defaultSystem() {
        return """
                你是一名企业内部智能问答助手，基于给定的内部知识片段回答问题。
                要求：只依据提供的知识片段作答，严禁编造；知识片段不足时明确说明未检索到相关信息。
                使用简体中文，结构清晰。""";
    }

    private String defaultUser() {
        return """
                【知识片段】
                {context}

                【用户问题】
                {question}
                """;
    }
}
