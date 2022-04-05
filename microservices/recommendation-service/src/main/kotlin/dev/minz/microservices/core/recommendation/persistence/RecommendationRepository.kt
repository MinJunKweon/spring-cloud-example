package dev.minz.microservices.core.recommendation.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface RecommendationRepository : ReactiveMongoRepository<RecommendationEntity, String> {
    fun findByProductId(productId: Int): Flux<RecommendationEntity>
}
