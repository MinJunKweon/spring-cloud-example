package dev.minz.microservices.core.product.services

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.microservices.core.product.persistence.ProductRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import dev.minz.util.http.ServiceUtil
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController

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
        val savedProduct = runBlocking {
            val result = try {
                repository.save(entity)
            } catch (e: DuplicateKeyException) {
                throw InvalidInputException("Duplicate key, product id: ${body.productId}")
            }
            mapper.entityToApi(result)
        }
        return savedProduct
    }

    override suspend fun getProduct(productId: Int): Product {
        require(productId > 0) {
            throw InvalidInputException("Invalid productId: $productId")
        }
        val productEntity = repository.findByProductId(productId)
            ?: throw NotFoundException("No product found for productId: $productId")

        val product = mapper.entityToApi(productEntity).apply {
            serviceAddress = serviceUtil.serviceAddress
        }
        return product
    }

    override fun deleteProduct(productId: Int) {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }
        LOG.debug("deleteProduct: tries to delete an entity with product: $productId")
        runBlocking {
            repository.findByProductId(productId)?.let { repository.delete(it) }
        }
    }
}
