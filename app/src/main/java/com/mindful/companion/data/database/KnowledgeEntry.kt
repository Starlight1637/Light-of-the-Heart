package com.mindful.companion.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 心理健康知识库条目
 * 存储本地核心心理健康知识，用于离线RAG检索
 */
@Entity(tableName = "knowledge_entries")
data class KnowledgeEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** 知识分类: CBT, EMOTION, CRISIS, STUDY, SLEEP, SOCIAL, MINDFULNESS, RESOURCE */
    @ColumnInfo(name = "category")
    val category: String,

    /** 条目标题（简短描述） */
    @ColumnInfo(name = "title")
    val title: String,

    /** 知识正文 */
    @ColumnInfo(name = "content")
    val content: String,

    /** 关键词，逗号分隔，用于匹配查询 */
    @ColumnInfo(name = "keywords")
    val keywords: String,

    /** 来源/参考 */
    @ColumnInfo(name = "source")
    val source: String = "心光知识库",

    /** 适用风险等级: LOW, MEDIUM, HIGH, CRITICAL, ALL */
    @ColumnInfo(name = "risk_level_hint")
    val riskLevelHint: String = "ALL"
)
