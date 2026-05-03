package com.mindful.companion.data.repository

import com.mindful.companion.data.api.*
import com.mindful.companion.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val apiService: MindfulApiService,
    private val ragManager: RAGManager,
    private val authRepository: AuthRepository
) {

    suspend fun analyzeEmotion(text: String): Result<com.mindful.companion.data.model.EmotionAnalysis> {
        return try {
            val request = com.mindful.companion.data.api.EmotionAnalysisRequest(text)
            val response = apiService.analyzeEmotion(request)

            if (response.isSuccessful) {
                response.body()?.let { analysis ->
                    Result.success(analysis)
                } ?: Result.failure(Exception("Empty response"))
            } else {
                val offlineAnalysis = performOfflineEmotionAnalysis(text)
                Result.success(offlineAnalysis)
            }
        } catch (e: Exception) {
            val offlineAnalysis = performOfflineEmotionAnalysis(text)
            Result.success(offlineAnalysis)
        }
    }

    /**
     * 生成AI回复，集成CRAG+Self-RAG知识增强
     *
     * 流程：
     * 1. 用 RAGManager 检索相关心理健康知识（本地优先，远端增强）
     * 2. 尝试通过后端RAG生成接口获取知识增强回复
     * 3. 如后端不可用，使用检索到的本地知识拼接离线回复
     * 4. 最终兜底：纯离线关键词回复
     */
    suspend fun generateAIResponse(
        postContent: String,
        emotionAnalysis: com.mindful.companion.data.model.EmotionAnalysis,
        riskLevel: RiskLevel
    ): Result<AIResponse> {
        // 确保知识库已初始化
        ragManager.ensureKnowledgeBaseSeeded()

        // 检索相关知识
        val retrieval = ragManager.retrieve(
            query = postContent,
            riskLevel = riskLevel.name,
            topK = 5
        )

        // 尝试后端RAG生成
        if (retrieval.docs.isNotEmpty()) {
            val ragResult = ragManager.generateWithRAG(
                userMessage = postContent,
                riskLevel = riskLevel.name,
                retrievedDocs = retrieval.docs
            )
            ragResult.onSuccess { ragResponse ->
                return Result.success(
                    buildAIResponseFromRAG(ragResponse, riskLevel, retrieval.docs)
                )
            }
        }

        // 本地知识增强离线回复
        if (retrieval.docs.isNotEmpty()) {
            return Result.success(
                generateLocalKnowledgeEnhancedResponse(riskLevel, retrieval.docs)
            )
        }

        // 最终兜底：原始离线回复
        return try {
            val request = AIResponseRequest(postContent, emotionAnalysis, riskLevel)
            val response = apiService.generateAIResponse(request)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.success(generateOfflineResponse(riskLevel))
            } else {
                Result.success(generateOfflineResponse(riskLevel))
            }
        } catch (e: Exception) {
            Result.success(generateOfflineResponse(riskLevel))
        }
    }

    suspend fun generateChatResponse(
        userMessage: String,
        sessionHistory: List<SimpleMessage> = emptyList()
    ): Result<AIResponse> {
        val emotionAnalysis = analyzeEmotion(userMessage).getOrElse {
            performOfflineEmotionAnalysis(userMessage)
        }
        val riskLevel = inferRiskLevel(userMessage, emotionAnalysis)

        ragManager.ensureKnowledgeBaseSeeded()
        val retrieval = ragManager.retrieve(
            query = userMessage,
            riskLevel = riskLevel.name,
            topK = 5
        )

        if (retrieval.docs.isNotEmpty()) {
            val ragResult = ragManager.generateWithRAG(
                userMessage = userMessage,
                riskLevel = riskLevel.name,
                retrievedDocs = retrieval.docs,
                sessionHistory = sessionHistory.takeLast(8)
            )
            ragResult.onSuccess { ragResponse ->
                return Result.success(
                    buildAIResponseFromRAG(ragResponse, riskLevel, retrieval.docs)
                )
            }
        }

        if (retrieval.docs.isNotEmpty()) {
            return Result.success(
                generateLocalChatKnowledgeResponse(userMessage, riskLevel, retrieval.docs)
            )
        }

        return Result.success(generateOfflineResponse(riskLevel))
    }

    suspend fun sendEmergencyAlert(
        postId: String,
        riskLevel: RiskLevel,
        content: String
    ): Result<Unit> {
        return try {
            val token = authRepository.getToken()
                ?: return Result.failure(Exception("未登录"))
            val alert = EmergencyAlert(
                postId = postId,
                riskLevel = riskLevel,
                content = content,
                timestamp = System.currentTimeMillis()
            )
            val response = apiService.sendEmergencyAlert(
                "Bearer $token",
                alert
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to send alert: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------------------
    // 内部：基于RAG后端响应构建AIResponse
    // ----------------------------------------------------------------

    private fun buildAIResponseFromRAG(
        ragResponse: RAGGenerateResponse,
        riskLevel: RiskLevel,
        docs: List<RAGRetrievedDoc>
    ): AIResponse {
        val resources = mutableListOf<Resource>()

        // 危机场景：强制附加热线
        if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH) {
            resources.add(Resource("心理危机援助热线", "24小时专业支持 · 400-161-9995", ResourceType.HOTLINE, "400-161-9995"))
            resources.add(Resource("校园心理咨询中心", "预约专业心理咨询", ResourceType.COUNSELING_CENTER))
        }

        val sourceTitles = docs.take(3).map { it.title }

        return AIResponse(
            message = ragResponse.message,
            suggestions = emptyList(),
            resources = resources,
            sources = sourceTitles
        )
    }

    // ----------------------------------------------------------------
    // 内部：本地知识增强回复
    // ----------------------------------------------------------------

    private fun generateLocalKnowledgeEnhancedResponse(
        riskLevel: RiskLevel,
        docs: List<RAGRetrievedDoc>
    ): AIResponse {
        val baseMessage = generateOfflineBaseMessage(riskLevel)
        val knowledgeTip = docs.firstOrNull()?.let { doc ->
            "\n\n💡 **${doc.title}**\n${doc.content.take(120)}…\n（来源：${doc.source}）"
        } ?: ""

        val resources = mutableListOf<Resource>()
        if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH) {
            resources.add(Resource("心理危机援助热线", "24小时专业支持 · 400-161-9995", ResourceType.HOTLINE, "400-161-9995"))
            resources.add(Resource("校园心理咨询中心", "预约专业心理咨询", ResourceType.COUNSELING_CENTER))
        }

        val sourceTitles = docs.take(3).map { it.title }

        return AIResponse(
            message = baseMessage + knowledgeTip,
            suggestions = emptyList(),
            resources = resources,
            sources = sourceTitles
        )
    }

    private fun generateLocalChatKnowledgeResponse(
        userMessage: String,
        riskLevel: RiskLevel,
        docs: List<RAGRetrievedDoc>
    ): AIResponse {
        val doc = docs.first()
        val opener = when {
            userMessage.contains("睡") || userMessage.contains("失眠") -> listOf(
                "听起来你最近休息得很不踏实，这种消耗会让情绪也更难稳住。",
                "睡不好真的会把人拖得很累，先别急着责怪自己。"
            ).random()
            userMessage.contains("考试") || userMessage.contains("学习") || userMessage.contains("压力") -> listOf(
                "你现在像是被学业压力推着走，难怪会觉得紧绷。",
                "这种压力不是一句“放轻松”就能解决的，我们先把它拆小一点。"
            ).random()
            userMessage.contains("朋友") || userMessage.contains("同学") || userMessage.contains("关系") -> listOf(
                "人际关系卡住的时候，会让人特别怀疑自己。",
                "和别人相处带来的委屈和消耗，是很真实的。"
            ).random()
            riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH -> listOf(
                "我会很认真地看待你刚才说的这些，因为这听起来已经不只是普通难过了。",
                "你现在承受的东西很重，先让自己不要一个人硬扛。"
            ).random()
            else -> listOf(
                "我听到你在努力描述自己的状态，这本身就很重要。",
                "你愿意把这些说出来，说明你已经在尝试照顾自己了。"
            ).random()
        }

        val tip = doc.content
            .replace("\n", "")
            .take(120)
        val message = buildString {
            append(opener)
            append("\n\n")
            append("可以先试试「${doc.title}」里提到的一个小步骤：")
            append(tip)
            append(if (doc.content.length > 120) "…" else "")
            if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH) {
                append("\n\n如果你担心自己会伤害自己，请现在联系身边可信任的人，或拨打心理援助热线 400-161-9995。")
            } else {
                append("\n\n你也可以继续跟我说，刚才这件事里最让你难受的是哪一部分。")
            }
        }

        val resources = mutableListOf<Resource>()
        if (riskLevel == RiskLevel.CRITICAL || riskLevel == RiskLevel.HIGH) {
            resources.add(Resource("心理危机援助热线", "24小时专业支持 · 400-161-9995", ResourceType.HOTLINE, "400-161-9995"))
            resources.add(Resource("校园心理咨询中心", "预约专业心理咨询", ResourceType.COUNSELING_CENTER))
        }

        return AIResponse(
            message = message,
            suggestions = emptyList(),
            resources = resources,
            sources = docs.take(3).map { it.title }
        )
    }

    // ----------------------------------------------------------------
    // 内部：离线情绪分析
    // ----------------------------------------------------------------

    private fun performOfflineEmotionAnalysis(text: String): com.mindful.companion.data.model.EmotionAnalysis {
        val sadKeywords = listOf("难过", "伤心", "痛苦", "绝望", "抑郁", "沮丧")
        val anxietyKeywords = listOf("焦虑", "紧张", "担心", "害怕", "恐惧", "不安")
        val angerKeywords = listOf("愤怒", "生气", "恼火", "烦躁", "愤恨")
        val riskKeywords = listOf("自杀", "结束", "不想活", "死", "伤害自己", "绝望")

        val lowerText = text.lowercase()

        val sadness = sadKeywords.count { lowerText.contains(it) } / 10.0f
        val anxiety = anxietyKeywords.count { lowerText.contains(it) } / 10.0f
        val anger = angerKeywords.count { lowerText.contains(it) } / 10.0f
        val foundRiskKeywords = riskKeywords.filter { lowerText.contains(it) }

        val overall = 1.0f - (sadness + anxiety + anger) / 3.0f

        return com.mindful.companion.data.model.EmotionAnalysis(
            happiness = maxOf(0f, overall),
            sadness = minOf(1f, sadness),
            anger = minOf(1f, anger),
            fear = 0f,
            anxiety = minOf(1f, anxiety),
            overall = maxOf(0f, overall),
            riskKeywords = foundRiskKeywords
        )
    }

    private fun inferRiskLevel(
        text: String,
        analysis: com.mindful.companion.data.model.EmotionAnalysis
    ): RiskLevel {
        val criticalKeywords = listOf("自杀", "轻生", "结束生命", "不想活", "活不下去", "伤害自己", "割腕", "跳楼")
        val highKeywords = listOf("绝望", "崩溃", "撑不住", "痛苦", "麻木", "恐慌")

        return when {
            criticalKeywords.any { text.contains(it) } || analysis.riskKeywords.any { keyword ->
                criticalKeywords.any { keyword.contains(it) || it.contains(keyword) }
            } -> RiskLevel.CRITICAL
            highKeywords.any { text.contains(it) } || analysis.overall < 0.3f -> RiskLevel.HIGH
            analysis.overall < 0.5f || analysis.sadness > 0.25f || analysis.anxiety > 0.25f -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    // ----------------------------------------------------------------
    // 内部：离线基础回复
    // ----------------------------------------------------------------

    private fun generateOfflineBaseMessage(riskLevel: RiskLevel): String = when (riskLevel) {
        RiskLevel.CRITICAL -> "嘿，我能感觉到你现在真的很不好受。我想让你知道，你不是一个人在面对这些，真的。有些时候，我们都需要别人的帮助，这一点也不丢人。如果你现在感觉特别难熬，可以打这个电话 400-161-9995，那边有专业的人24小时在线，他们真的很愿意听你说话。"
        RiskLevel.HIGH -> "听起来你最近过得挺不容易的。我想说，有这些感受是很正常的，每个人都会有这样的时候。你愿意试试做几次深呼吸吗？有时候这样能让心情平静一些。如果感觉自己应付不来，找专业的心理咨询师聊聊也是个好主意。"
        RiskLevel.MEDIUM -> "我能理解你现在的感受。其实每个人都会有情绪低落的时候，这真的很正常。这种时候，好好照顾自己就特别重要了。试着保持规律的作息，早睡早起会让人感觉好一些。"
        RiskLevel.LOW -> "谢谢你愿意跟我分享这些。你知道吗，每一天都是新的开始。你比自己想象的要坚强，也有能力去面对那些挑战。记得多留意生活中那些美好的小事。"
    }

    private fun generateOfflineResponse(riskLevel: RiskLevel): AIResponse {
        return when (riskLevel) {
            RiskLevel.CRITICAL -> AIResponse(
                message = generateOfflineBaseMessage(riskLevel),
                suggestions = listOf(),
                resources = listOf(
                    Resource("心理危机干预热线", "24小时专业心理支持", ResourceType.HOTLINE, "400-161-9995"),
                    Resource("校园心理咨询中心", "专业心理咨询服务", ResourceType.COUNSELING_CENTER)
                )
            )
            RiskLevel.HIGH -> AIResponse(
                message = generateOfflineBaseMessage(riskLevel),
                suggestions = listOf()
            )
            RiskLevel.MEDIUM -> AIResponse(
                message = generateOfflineBaseMessage(riskLevel),
                suggestions = listOf()
            )
            RiskLevel.LOW -> AIResponse(
                message = generateOfflineBaseMessage(riskLevel),
                suggestions = listOf()
            )
        }
    }
}
