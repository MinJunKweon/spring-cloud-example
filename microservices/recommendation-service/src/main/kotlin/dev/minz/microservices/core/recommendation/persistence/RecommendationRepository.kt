package dev.minz.microservices.core.recommendation.persistence

import org.springframework.data.repository.CrudRepository

interface RecommendationRepository : CrudRepository<RecommendationEntity, String> {
    fun findByProductId(productId: Int): List<RecommendationEntity>
}
