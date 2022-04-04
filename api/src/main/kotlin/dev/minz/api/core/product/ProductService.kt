package dev.minz.api.core.product

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

interface ProductService {

    @PostMapping(
        value = ["/product"],
        consumes = [APPLICATION_JSON],
        produces = [APPLICATION_JSON],
    )
    fun createProduct(@RequestBody body: Product): Product

    @GetMapping(
        value = ["/product/{productId}"],
        produces = [APPLICATION_JSON],
    )
    fun getProduct(@PathVariable productId: Int): Product?

    @DeleteMapping(value = ["/product/{productId}"])
    fun deleteProduct(@PathVariable productId: Int)
}
