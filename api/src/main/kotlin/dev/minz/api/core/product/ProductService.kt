package dev.minz.api.core.product

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Mono

interface ProductService {

    fun createProduct(@RequestBody body: Product): Product

    @GetMapping(
        value = ["/product/{productId}"],
        produces = [APPLICATION_JSON],
    )
    fun getProduct(@PathVariable productId: Int): Mono<Product>

    fun deleteProduct(@PathVariable productId: Int)
}
