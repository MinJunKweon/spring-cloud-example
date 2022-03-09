package dev.minz.api.core.product

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface ProductService {
    @GetMapping(
        value = ["/product/{productId}"],
        produces = ["application/json"],
    )
    fun getProduct(@PathVariable productId: Int): Product?
}
