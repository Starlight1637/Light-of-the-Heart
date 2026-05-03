package com.mindful.companion.data.api

import com.mindful.companion.data.model.RAGGenerateRequest
import com.mindful.companion.data.model.RAGGenerateResponse
import com.mindful.companion.data.model.RAGRetrieveRequest
import com.mindful.companion.data.model.RAGRetrieveResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * RAG 后端服务接口（CRAG + Self-RAG）
 * 对应 backend/api/rag_endpoints.py
 */
interface RAGApiService {

    /**
     * 检索相关心理健康知识
     * POST /api/rag/retrieve
     */
    @POST("api/rag/retrieve")
    suspend fun retrieve(
        @Header("Authorization") token: String,
        @Body request: RAGRetrieveRequest
    ): Response<RAGRetrieveResponse>

    /**
     * 基于检索结果生成支持性回复
     * POST /api/rag/generate
     */
    @POST("api/rag/generate")
    suspend fun generate(
        @Header("Authorization") token: String,
        @Body request: RAGGenerateRequest
    ): Response<RAGGenerateResponse>
}
