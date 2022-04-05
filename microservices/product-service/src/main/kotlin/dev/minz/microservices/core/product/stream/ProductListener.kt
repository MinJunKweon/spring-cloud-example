package dev.minz.microservices.core.product.stream

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.api.event.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message

@Configuration
class ProductListener(
    private val productService: ProductService,
) {
    @Bean
    fun process(): (Message<Event<Int, Product>>) -> Unit = {
        val payload = it.payload
        when (payload.eventType) {
            Event.Type.CREATE -> productService.createProduct(payload.data)
            Event.Type.DELETE -> productService.deleteProduct(payload.key)
        }
    }
}
