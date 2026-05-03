package com.mindful.companion.data.model

data class AIResponse(
    val message: String,
    val emotion: String? = null,
    val riskLevel: String? = null,
    val suggestions: List<String> = emptyList(),
    val resources: List<Resource> = emptyList(),
    /** 知识来源引用（来自RAG检索的知识条目标题） */
    val sources: List<String> = emptyList()
)
