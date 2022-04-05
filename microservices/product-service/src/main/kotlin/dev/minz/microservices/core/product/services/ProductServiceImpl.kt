package dev.minz.microservices.core.product.services

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.microservices.core.product.persistence.ProductRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import dev.minz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@RestController
class ProductServiceImpl(
    private val repository: ProductRepository,
    private val mapper: ProductMapper,
    private val serviceUtil: ServiceUtil,
) : ProductService {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductServiceImpl::class.java)
    }

    override fun createProduct(body: Product): Product {
        val entity = mapper.apiToEntity(body)
        val savedProduct = repository.save(entity)
            .log()
            .onErrorMap(DuplicateKeyException::class.java) {
                InvalidInputException("Duplicate key, product id: ${body.productId}")
            }
            .map { mapper.entityToApi(it) }
            .block()
        return checkNotNull(savedProduct) { "Product must be not null. Product Id: ${body.productId}" }
    }

    override fun getProduct(productId: Int): Mono<Product> {
        require(productId > 0) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        return repository.findByProductId(productId)
            .switchIfEmpty { Mono.error(NotFoundException("No product found for productId: $productId")) }
            .log()
            .map { mapper.entityToApi(it) }
            .map {
                it.serviceAddress = serviceUtil.serviceAddress
                it
            }
    }

    override fun deleteProduct(productId: Int) {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }
        LOG.debug("deleteProduct: tries to delete an entity with product: $productId")
        repository.findByProductId(productId).log().map {
            repository.delete(it)
        }.flatMap { it }.block()
    }
}
