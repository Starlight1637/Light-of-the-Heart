package com.mindful.companion.data.model

data class Resource(
    val title: String,
    val description: String,
    val type: ResourceType,
    val contact: String? = null,
    val url: String? = null
)

enum class ResourceType {
    HOTLINE,
    COUNSELING,
    COUNSELING_CENTER,
    ARTICLE,
    VIDEO,
    MEDITATION
}
