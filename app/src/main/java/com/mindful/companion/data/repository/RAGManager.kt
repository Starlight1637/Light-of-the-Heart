package com.mindful.companion.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.mindful.companion.data.api.RAGApiService
import com.mindful.companion.data.database.KnowledgeDao
import com.mindful.companion.data.database.KnowledgeEntry
import com.mindful.companion.data.database.KnowledgeSeedData
import com.mindful.companion.data.model.HybridRetrievalResult
import com.mindful.companion.data.model.RAGGenerateRequest
import com.mindful.companion.data.model.RAGGenerateResponse
import com.mindful.companion.data.model.RAGRetrieveRequest
import com.mindful.companion.data.model.RAGRetrievedDoc
import com.mindful.companion.data.model.SimpleMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * RAG 混合检索管理器
 *
 * 策略：
 *  1. 优先本地知识库（FTS + LIKE 检索，零延迟，离线可用）
 *  2. 有网络时，并行请求远端 CRAG+Self-RAG 后端
 *  3. 合并结果，按相关性排序，去重
 *  4. 如果远端不可用，纯本地降级
 */
@Singleton
class RAGManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val knowledgeDao: KnowledgeDao,
    private val ragApiService: RAGApiService,
    private val authRepository: AuthRepository
) {

    // ----------------------------------------------------------------
    // 初始化：首次运行时植入种子数据
    // ----------------------------------------------------------------

    suspend fun ensureKnowledgeBaseSeeded() {
        if (knowledgeDao.count() == 0) {
            knowledgeDao.insertAll(KnowledgeSeedData.getEntries())
        }
    }

    // ----------------------------------------------------------------
    // 混合检索
    // ----------------------------------------------------------------

    suspend fun retrieve(
        query: String,
        riskLevel: String = "LOW",
        topK: Int = 5
    ): HybridRetrievalResult {
        val localDocs = retrieveLocal(query, riskLevel, topK)

        if (!isNetworkAvailable()) {
            return HybridRetrievalResult(
                docs = localDocs,
                isFromLocalOnly = true,
                isFromRemoteOnly = false,
                isMixed = false
            )
        }

        val remoteDocs = retrieveRemote(query, riskLevel, topK)

        return if (remoteDocs.isEmpty()) {
            HybridRetrievalResult(
                docs = localDocs,
                isFromLocalOnly = true,
                isFromRemoteOnly = false,
                isMixed = false
            )
        } else {
            val merged = mergeDocs(localDocs, remoteDocs, topK)
            HybridRetrievalResult(
                docs = merged,
                isFromLocalOnly = false,
                isFromRemoteOnly = localDocs.isEmpty(),
                isMixed = localDocs.isNotEmpty() && remoteDocs.isNotEmpty()
            )
        }
    }

    // ----------------------------------------------------------------
    // RAG 生成（委托后端）
    // ----------------------------------------------------------------

    suspend fun generateWithRAG(
        userMessage: String,
        riskLevel: String,
        retrievedDocs: List<RAGRetrievedDoc>,
        sessionHistory: List<SimpleMessage>? = null
    ): Result<RAGGenerateResponse> {
        if (!isNetworkAvailable() || retrievedDocs.isEmpty()) {
            return Result.failure(Exception("offline_or_no_docs"))
        }
        return try {
            val request = RAGGenerateRequest(
                userMessage = userMessage,
                riskLevel = riskLevel,
                retrievedDocs = retrievedDocs,
                sessionHistory = sessionHistory
            )
            val token = authRepository.getToken()
                ?: return Result.failure(Exception("not_logged_in"))
            val response = ragApiService.generate("Bearer $token", request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(Exception("Empty RAG response"))
            } else {
                Result.failure(Exception("RAG generate failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // 内部：本地检索
    // ----------------------------------------------------------------

    private suspend fun retrieveLocal(
        query: String,
        riskLevel: String,
        topK: Int
    ): List<RAGRetrievedDoc> {
        // 将查询分词（按中文特点拆分为2字以上的片段）
        val tokens = tokenizeQueryEnhanced(query)
        val scored = linkedMapOf<Long, Pair<KnowledgeEntry, Float>>()

        // 对每个词片段搜索
        for ((index, token) in tokens.withIndex()) {
            val hits = knowledgeDao.search(token, limit = topK)
            hits.forEach { entry ->
                val previous = scored[entry.id]?.second ?: 0f
                scored[entry.id] = entry to (previous + scoreLocalHit(entry, token, index))
            }
        }

        // 高风险：追加危机相关条目
        if (riskLevel in listOf("HIGH", "CRITICAL")) {
            knowledgeDao.getCrisisEntries(limit = 3).forEach { entry ->
                val previous = scored[entry.id]?.second ?: 0f
                scored[entry.id] = entry to (previous + 3.0f)
            }
        }

        return scored.values
            .sortedByDescending { it.second }
            .take(topK)
            .mapIndexed { index, entry ->
                entry.first.toRAGDoc(relevanceScore = (0.95f - index * 0.05f).coerceAtLeast(0.5f))
            }
    }

    // ----------------------------------------------------------------
    // 内部：远端检索
    // ----------------------------------------------------------------

    private suspend fun retrieveRemote(
        query: String,
        riskLevel: String,
        topK: Int
    ): List<RAGRetrievedDoc> {
        return try {
            val request = RAGRetrieveRequest(
                query = query,
                riskLevel = riskLevel,
                topK = topK
            )
            val token = authRepository.getToken() ?: return emptyList()
            val response = ragApiService.retrieve("Bearer $token", request)
            if (response.isSuccessful) {
                response.body()?.docs ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ----------------------------------------------------------------
    // 内部：合并与排序
    // ----------------------------------------------------------------

    private fun mergeDocs(
        local: List<RAGRetrievedDoc>,
        remote: List<RAGRetrievedDoc>,
        topK: Int
    ): List<RAGRetrievedDoc> {
        val seen = mutableSetOf<String>()
        val merged = mutableListOf<RAGRetrievedDoc>()

        // 交替融合：remote (高质量) 优先，但保留本地唯一条目
        val remoteNormalized = remote.map { it.copy(relevanceScore = it.relevanceScore * 1.1f) }
        val all = (remoteNormalized + local).sortedByDescending { it.relevanceScore }

        for (doc in all) {
            val key = doc.title.take(20)
            if (seen.add(key)) {
                merged.add(doc)
                if (merged.size >= topK) break
            }
        }
        return merged
    }

    // ----------------------------------------------------------------
    // 工具
    // ----------------------------------------------------------------

    private fun tokenizeQuery(query: String): List<String> {
        // 简单分词：提取2字以上的中文词汇片段
        val result = mutableListOf<String>()
        if (query.length >= 2) result.add(query.take(10))

        // 按常见标点拆分
        val parts = query.split("，", "。", "？", "！", "、", " ", ",", ".")
        parts.filter { it.length >= 2 }.forEach { result.add(it.take(10)) }

        return result.distinct()
    }

    private fun tokenizeQueryEnhanced(query: String): List<String> {
        val normalized = query
            .lowercase()
            .replace(Regex("[\\p{Punct}\\s，。！？、；：,.!?;:]+"), " ")
            .trim()
        val result = linkedSetOf<String>()

        normalized.split(" ")
            .filter { it.length >= 2 }
            .forEach { part ->
                result.add(part.take(12))
                addChineseWindows(part, result)
            }

        val whole = normalized.replace(" ", "")
        if (whole.length >= 2) {
            addKnownMentalHealthKeywords(whole, result)
            addChineseWindows(whole, result)
        }

        return result.filter { it.length >= 2 }.take(40)
    }

    private fun addKnownMentalHealthKeywords(text: String, result: MutableSet<String>) {
        val keywords = listOf(
            "焦虑", "担心", "未来", "压力", "考试", "学习", "失眠", "睡眠",
            "难过", "低落", "抑郁", "孤独", "关系", "社交", "自责", "内疚",
            "控制", "放松", "呼吸", "正念", "危机", "自伤", "自杀", "求助"
        )
        keywords.filter { text.contains(it) }.forEach { result.add(it) }
    }

    private fun addChineseWindows(text: String, result: MutableSet<String>) {
        val chineseOnly = text.filter { it in '\u4e00'..'\u9fff' }
        for (size in 2..4) {
            if (chineseOnly.length < size) continue
            for (index in 0..(chineseOnly.length - size)) {
                result.add(chineseOnly.substring(index, index + size))
                if (result.size >= 40) return
            }
        }
    }

    private fun scoreLocalHit(entry: KnowledgeEntry, token: String, tokenIndex: Int): Float {
        val locationWeight = when {
            entry.title.contains(token, ignoreCase = true) -> 3.0f
            entry.keywords.contains(token, ignoreCase = true) -> 2.0f
            entry.content.contains(token, ignoreCase = true) -> 1.0f
            else -> 0.5f
        }
        return locationWeight / (1 + tokenIndex * 0.08f)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun KnowledgeEntry.toRAGDoc(relevanceScore: Float) = RAGRetrievedDoc(
        id = id.toString(),
        title = title,
        content = content,
        category = category,
        relevanceScore = relevanceScore,
        source = source
    )
}
