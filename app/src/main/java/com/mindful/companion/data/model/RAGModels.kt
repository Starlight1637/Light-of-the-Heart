package com.mindful.companion.data.model

import com.google.gson.annotations.SerializedName

/**
 * RAG 检索增强请求/响应数据模型
 */

data class RAGRetrieveRequest(
    val query: String,
    @SerializedName("risk_level")
    val riskLevel: String = "LOW",
    @SerializedName("top_k")
    val topK: Int = 5,
    val categories: List<String>? = null
)

data class RAGRetrievedDoc(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    @SerializedName("relevance_score")
    val relevanceScore: Float,
    val source: String = "知识库"
)

data class RAGRetrieveResponse(
    val docs: List<RAGRetrievedDoc>,
    @SerializedName("query_rewritten")
    val queryRewritten: String? = null,
    @SerializedName("retrieval_strategy")
    val retrievalStrategy: String = "hybrid"
)

data class RAGGenerateRequest(
    @SerializedName("user_message")
    val userMessage: String,
    @SerializedName("risk_level")
    val riskLevel: String,
    @SerializedName("retrieved_docs")
    val retrievedDocs: List<RAGRetrievedDoc>,
    @SerializedName("session_history")
    val sessionHistory: List<SimpleMessage>? = null
)

data class SimpleMessage(
    val role: String,   // "user" or "assistant"
    val content: String
)

data class RAGGenerateResponse(
    val message: String,
    @SerializedName("sources_used")
    val sourcesUsed: List<String> = emptyList(),
    val confidence: Float = 1.0f,
    @SerializedName("self_rag_passed")
    val selfRagPassed: Boolean = true
)

/** 混合检索结果：本地 + 远程合并 */
data class HybridRetrievalResult(
    val docs: List<RAGRetrievedDoc>,
    val isFromLocalOnly: Boolean,
    val isFromRemoteOnly: Boolean,
    val isMixed: Boolean
)
