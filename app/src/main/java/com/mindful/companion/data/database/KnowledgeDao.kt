package com.mindful.companion.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface KnowledgeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<KnowledgeEntry>)

    /** 全文检索：在 title + content + keywords 中搜索关键词 */
    @Query("""
        SELECT * FROM knowledge_entries
        WHERE title LIKE '%' || :query || '%'
           OR content LIKE '%' || :query || '%'
           OR keywords LIKE '%' || :query || '%'
        LIMIT :limit
    """)
    suspend fun search(query: String, limit: Int = 5): List<KnowledgeEntry>

    /** 按分类 + 风险等级检索 */
    @Query("""
        SELECT * FROM knowledge_entries
        WHERE category = :category
          AND (risk_level_hint = :riskLevel OR risk_level_hint = 'ALL')
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun searchByCategory(category: String, riskLevel: String, limit: Int = 3): List<KnowledgeEntry>

    /** 危机条目：专门用于高风险响应 */
    @Query("""
        SELECT * FROM knowledge_entries
        WHERE risk_level_hint IN ('HIGH', 'CRITICAL')
        ORDER BY RANDOM()
        LIMIT :limit
    """)
    suspend fun getCrisisEntries(limit: Int = 3): List<KnowledgeEntry>

    @Query("SELECT COUNT(*) FROM knowledge_entries")
    suspend fun count(): Int

    @Query("DELETE FROM knowledge_entries")
    suspend fun deleteAll()
}
