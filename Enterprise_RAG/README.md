# 企业级 AI Agent 平台与智能问答系统（enterprise-qa）

基于大语言模型（LLM）构建的企业内部智能问答 Agent，采用 **RAG 混合检索**（关键词 + 可插拔向量），围绕
**Prompt 规则沉淀 → 知识检索 → 答案生成 → 效果评测** 形成完整闭环，并提供 Web 问答界面与 RESTful API。

> 部署说明：本仓库代码由助手一次性生成，**未在生成环境编译运行**，请在装好 JDK 17 / Maven / MySQL 8 后按
> 下文执行一次 `mvn` 验证，运行截图与接口验证结果以你本地实际为准。

---

## 1. 技术栈

| 层 | 选型 |
|---|---|
| 语言 / 构建 | Java 17 · Maven |
| 框架 | Spring Boot 3.2（Web / Data JPA / Validation） |
| 数据库 | MySQL 8（utf8mb4，FULLTEXT ngram 中文全文索引） |
| LLM | DeepSeek（OpenAI 兼容 Chat Completions），接口抽象可扩展多厂商 |
| 检索 | MySQL FULLTEXT 关键词 + 可插拔向量语义（默认不启用，开箱即跑） |
| 前端 | 原生 HTML/JS 单页问答界面（`static/index.html`） |

## 2. 目录结构

```
enterprise-qa/
├── pom.xml
├── README.md
├── sql/
│   ├── schema.sql            # 建库建表 + 索引 + 全文索引
│   └── data.sql              # 种子知识文档（考勤/报销/IT/知识库/入职）
└── src/main/
    ├── java/com/enterprise/qa/
    │   ├── QaApplication.java
    │   ├── config/           # WebConfig(CORS)、DataInitializer(自动切块)
    │   ├── common/           # Result、BusinessException、全局异常处理
    │   ├── controller/       # Chat / Document / Record
    │   ├── dto/              # 请求与响应模型
    │   ├── entity/           # KnowledgeDocument / DocumentChunk / QaRecord
    │   ├── repository/       # JPA Repository
    │   ├── service/          # ChatService / DocumentService / RecordService
    │   ├── rag/              # RAGRetriever、KeywordRetriever、VectorRetriever、TextChunker
    │   ├── llm/              # LlmClient 接口 + DeepSeekClient
    │   └── prompt/           # PromptTemplate（Prompt 规则沉淀）
    └── resources/
        ├── application.yml   # 数据源 / LLM / RAG / Prompt 集中配置
        └── static/index.html # Web 问答界面
```

## 3. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.x（全文索引 ngram 依赖 MySQL 5.7.6+）
- 一个 DeepSeek API Key（可复用你其它项目的 `DEEPSEEK_API_KEY`）

## 4. 快速开始

### 4.1 初始化数据库

```bash
mysql -u root -p < sql/schema.sql   # 建库建表
mysql -u root -p < sql/data.sql     # 导入 5 篇种子知识文档
```

### 4.2 配置

编辑 `src/main/resources/application.yml`，或直接设置环境变量（优先级更高）：

```bash
export DEEPSEEK_API_KEY=sk-xxxx          # LLM Key（必填）
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_DATABASE=enterprise_qa
export MYSQL_USER=root
export MYSQL_PASSWORD=你的密码
```

### 4.3 编译运行

```bash
mvn clean package
java -jar target/enterprise-qa-1.0.0.jar
```

启动成功后访问 Web 界面：**http://localhost:8080/** （可输入问题直接问答）
首次启动时 `DataInitializer` 会自动把种子文档切块写入 `document_chunk`，日志会打印切块数量。

### 4.4 验证

```bash
# 健康检查（curl）
curl http://localhost:8080/api/records

# 直接调用问答接口
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"question":"差旅费的报销标准是什么？"}'
```

## 5. RESTful API

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/chat` | 智能问答，body `{"question":"..."}`，返回答案 + 来源 + 延迟 + token |
| POST | `/api/documents` | 新增知识文档（自动切块），body `{"title","category","content","source"}` |
| GET | `/api/documents` | 文档列表 |
| DELETE | `/api/documents/{id}` | 删除文档及其分块 |
| GET | `/api/records` | 最近问答记录（`?limit=20`），用于效果评测 |

统一返回结构：`{"code":0,"message":"ok","data":...}`，`code!=0` 为业务错误。

## 6. 核心设计：RAG 混合检索

**流程**：入库分块 → 问题检索 → Prompt 组装 → LLM 生成 → 记录评测数据。

1. **文本分块**（`TextChunker`）：按固定窗口 + 重叠，并尽量在句/段边界断句，保留语义完整性（`rag.chunk-size / chunk-overlap` 可调）。
2. **关键词检索**（`KeywordRetriever`）：`document_chunk.content` 建 **FULLTEXT 索引（ngram parser，天然支持中文）**，用
   `MATCH(...) AGAINST(... IN NATURAL LANGUAGE MODE)` 召回，配合 `JOIN` 拿到文档标题。
3. **向量检索**（`VectorRetriever` 接口）：默认 `Noop` 不启用；配置 `rag.vector.enabled=true` 并实现该接口即可接入
   OpenAI 兼容 Embedding 服务，与关键词构成**混合加权**（`rag.weights.keyword/vector`），开箱无需 Embedding 也能跑。
4. **融合排序**（`RAGRetriever`）：关键词与向量分数归一化后按权重线性融合，去重、按相关度降序取 `rag.top-k`。
5. **Prompt 组装**（`PromptTemplate`）：系统提示词约束"只依据知识片段作答、禁止编造、不足时明确说明"；模板集中在
   `application.yml` 的 `prompt` 节点，便于沉淀与评测不同 Prompt 版本。

**索引设计说明**（配合 `EXPLAIN` 优化）：

```sql
-- document_chunk 关键词检索：全文索引走 MATCH...AGAINST，避免全表扫描
EXPLAIN SELECT c.id, d.title, MATCH(c.content) AGAINST('报销' IN NATURAL LANGUAGE MODE) AS score
FROM document_chunk c JOIN knowledge_document d ON c.doc_id = d.id
WHERE MATCH(c.content) AGAINST('报销' IN NATURAL LANGUAGE MODE)
ORDER BY score DESC;

-- 辅助普通索引
idx_chunk_doc(doc_id)        -- 分块按文档查询 / 删除
idx_doc_category(category)   -- 文档分类筛选
idx_doc_created_at(created_at), idx_record_created_at(created_at)  -- 列表排序
```

## 7. 效果评测与链路追踪

每次问答都会写入 `qa_record`，沉淀 **问题、答案、模型、prompt/completion tokens、延迟、命中来源**，可用于：
- 分析响应延迟与 token 成本；
- 结合人工评估分析准确率与召回片段质量；
- 调优 `rag.top-k`、`rag.weights`、分块大小与 Prompt 模板，观察指标变化。

查看：`GET /api/records`，或直接查表：

```sql
SELECT id, question, model, latency_ms, prompt_tokens, completion_tokens, created_at
FROM qa_record ORDER BY created_at DESC LIMIT 20;
```

## 8. 常见问题

| 现象 | 处理 |
|---|---|
| 启动报数据库连接失败 | 确认 MySQL 已启动、账号密码与 `application.yml` 一致，`schema.sql` 已执行 |
| 问答返回"未配置 LLM API Key" | 设置环境变量 `DEEPSEEK_API_KEY` 后重启 |
| 检索不到内容 | 确认 `document_chunk` 有数据（首次启动自动切块）；`document_chunk.content` 全文索引需 ngram，确认 MySQL 8 |
| 全文索引不生效 | `SHOW INDEX FROM document_chunk;` 检查 `ft_chunk_content` 是否存在；中文检索依赖 ngram 分词 |

## 9. 简历对应描述（供参考）

本项目的设计要点与简历口径的映射：
- **RAG 混合检索** → 关键词（FULLTEXT ngram）+ 可插拔向量检索；
- **Prompt 规则沉淀** → `PromptTemplate` + 可配置模板，评测不同版本；
- **效果评测** → `qa_record` 沉淀延迟/token/来源，支撑准确率与延迟分析；
- **后端与数据模型** → 分层 RESTful API + MySQL 显式 schema 与索引（配合 Explain 优化）；
- **扩展点** → `LlmClient`（多厂商）、`VectorRetriever`（向量语义）均为接口，可扩展。
