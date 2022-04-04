package dev.minz.api.composite.product

data class RecommendationSummary(
    val recommendationId: Int,
    val author: String,
    val rate: Int,
    val content: String,
)
