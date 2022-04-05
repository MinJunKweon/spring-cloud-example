package dev.minz.microservices.core.product.persistence

import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Mono

interface ProductRepository : ReactiveMongoRepository<ProductEntity, String> {
    fun findByProductId(productId: Int): Mono<ProductEntity>
}
