package dev.minz.microservices.core.recommendation.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface RecommendationRepository : CoroutineCrudRepository<RecommendationEntity, String> {
    suspend fun findByProductId(productId: Int): List<RecommendationEntity>
}
