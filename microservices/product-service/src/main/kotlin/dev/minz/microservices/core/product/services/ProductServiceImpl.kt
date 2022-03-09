package dev.minz.microservices.core.product.services

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.util.http.ServiceUtil
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductServiceImpl(
    private val serviceUtil: ServiceUtil
) : ProductService {
    override fun getProduct(productId: Int): Product {
        return Product(
            productId = productId,
            name = "name-$productId",
            weight = 123,
            serviceAddress = serviceUtil.serviceAddress,
        )
    }
}
