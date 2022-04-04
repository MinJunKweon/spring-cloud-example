package dev.minz.microservices.core.product.configuration

import dev.minz.microservices.core.product.persistence.ProductEntity
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.index.IndexDefinition
import org.springframework.data.mongodb.core.index.IndexOperations
import org.springframework.data.mongodb.core.index.IndexResolver
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver

@Configuration
class MongoConfiguration(
    private val mongoTemplate: MongoOperations,
) {

    @EventListener(ContextRefreshedEvent::class)
    fun initIndicesAfterStartup() {
        val mappingContext = mongoTemplate.converter.mappingContext
        val resolver: IndexResolver = MongoPersistentEntityIndexResolver(mappingContext)
        val indexOps: IndexOperations = mongoTemplate.indexOps(ProductEntity::class.java)
        resolver.resolveIndexFor(ProductEntity::class.java).forEach { e: IndexDefinition? -> indexOps.ensureIndex(e!!) }
    }
}
