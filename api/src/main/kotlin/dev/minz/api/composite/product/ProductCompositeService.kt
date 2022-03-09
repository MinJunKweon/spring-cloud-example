package dev.minz.api.composite.product

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

interface ProductCompositeService {
    @GetMapping(
        value = ["/product-composite/{productId}"],
        produces = [APPLICATION_JSON]
    )
    fun getProduct(@PathVariable productId: Int): ProductAggregate
}
