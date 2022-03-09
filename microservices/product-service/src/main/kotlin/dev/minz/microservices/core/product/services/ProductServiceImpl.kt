package dev.minz.microservices.core.product.services

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import dev.minz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductServiceImpl(
    private val serviceUtil: ServiceUtil
) : ProductService {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductServiceImpl::class.java)
    }

    override fun getProduct(productId: Int): Product? {
        LOG.debug("/product return the found product for productId=$productId")

        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }
        require(productId != 13) { throw NotFoundException("No product found for productId: $productId") }

        return Product(
            productId = productId,
            name = "name-$productId",
            weight = 123,
            serviceAddress = serviceUtil.serviceAddress,
        )
    }
}
