package com.enterprise.qa.service;

import com.enterprise.qa.common.BusinessException;
import com.enterprise.qa.dto.ChatRequest;
import com.enterprise.qa.dto.ChatResponse;
import com.enterprise.qa.entity.QaRecord;
import com.enterprise.qa.llm.LlmClient;
import com.enterprise.qa.llm.LlmProperties;
import com.enterprise.qa.llm.LlmResult;
import com.enterprise.qa.prompt.PromptTemplate;
import com.enterprise.qa.rag.RAGRetriever;
import com.enterprise.qa.rag.RetrievedChunk;
import com.enterprise.qa.repository.QaRecordRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 问答核心链路：检索 → Prompt 组装 → LLM 生成 → 记录评测数据。
 */
@Service
public class ChatService {

    private final RAGRetriever retriever;
    private final LlmClient llmClient;
    private final LlmProperties llmProps;
    private final PromptTemplate promptTemplate;
    private final QaRecordRepository recordRepository;
    private final ObjectMapper objectMapper;

    public ChatService(RAGRetriever retriever, LlmClient llmClient, LlmProperties llmProps,
                       PromptTemplate promptTemplate, QaRecordRepository recordRepository,
                       ObjectMapper objectMapper) {
        this.retriever = retriever;
        this.llmClient = llmClient;
        this.llmProps = llmProps;
        this.promptTemplate = promptTemplate;
        this.recordRepository = recordRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ChatResponse chat(ChatRequest req) {
        if (llmProps.getApiKey() == null || llmProps.getApiKey().isBlank()) {
            throw new BusinessException(500, "未配置 LLM API Key，请在环境变量 DEEPSEEK_API_KEY 中设置");
        }
        long start = System.currentTimeMillis();

        // 1. RAG 混合检索
        List<RetrievedChunk> chunks = retriever.retrieve(req.question());

        // 2. 组装上下文与 Prompt
        String context = chunks.stream()
                .map(c -> "【来源】" + c.docTitle() + "\n" + c.content())
                .collect(Collectors.joining("\n\n---\n\n"));
        String system = promptTemplate.buildSystem();
        String user = promptTemplate.buildUser(context, req.question());

        // 3. LLM 生成
        LlmResult llm = llmClient.chat(system, user);
        long latency = System.currentTimeMillis() - start;

        // 4. 来源结构化（用于前端展示与审计）
        List<Map<String, Object>> sources = chunks.stream()
                .map(c -> Map.of(
                        "docId", c.docId(),
                        "title", c.docTitle(),
                        "content", c.content(),
                        "score", Math.round(c.score() * 10000) / 10000.0))
                .toList();

        // 5. 落库记录（供效果评测）
        saveRecord(req.question(), llm, latency, sources);

        return new ChatResponse(req.question(), llm.answer(), sources, latency,
                llmProps.getModel(), llm.promptTokens(), llm.completionTokens());
    }

    private void saveRecord(String question, LlmResult llm, long latency, List<Map<String, Object>> sources) {
        try {
            QaRecord rec = new QaRecord();
            rec.setQuestion(question);
            rec.setAnswer(llm.answer());
            rec.setModel(llmProps.getModel());
            rec.setPromptTokens(llm.promptTokens());
            rec.setCompletionTokens(llm.completionTokens());
            rec.setLatencyMs(latency);
            rec.setSources(objectMapper.writeValueAsString(sources));
            rec.setCreatedAt(LocalDateTime.now());
            recordRepository.save(rec);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "来源序列化失败: " + e.getMessage());
        }
    }
}
