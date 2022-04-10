package dev.minz.microservices.core.product.persistence

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ProductRepository : CoroutineCrudRepository<ProductEntity, String> {
    suspend fun findByProductId(productId: Int): ProductEntity?
}
