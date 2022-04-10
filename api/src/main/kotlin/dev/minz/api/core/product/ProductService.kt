package dev.minz.api.core.product

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

interface ProductService {

    fun createProduct(@RequestBody body: Product): Product

    @GetMapping(
        value = ["/product/{productId}"],
        produces = [APPLICATION_JSON],
    )
    suspend fun getProduct(@PathVariable productId: Int): Product

    fun deleteProduct(@PathVariable productId: Int)
}
