package dev.minz.microservices.core.product.stream

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.api.event.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ProductListener(
    private val productService: ProductService,
) {
    @Bean
    fun process(): (Event<Int, Product>) -> Unit = {
        when (it.eventType) {
            Event.Type.CREATE -> productService.createProduct(it.data)
            Event.Type.DELETE -> productService.deleteProduct(it.key)
        }
    }
}
