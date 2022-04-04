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
        val newEntity = try {
            repository.save(entity)
        } catch (dke: DuplicateKeyException) {
            throw InvalidInputException("Duplicate key, product id: ${body.productId}")
        }

        LOG.debug("createProduct: entity created for productId: ${body.productId}")
        return mapper.entityToApi(newEntity)
    }

    override fun getProduct(productId: Int): Product? {
        require(productId > 0) {
            throw InvalidInputException("Invalid productId: $productId")
        }

        val entity = checkNotNull(repository.findByProductId(productId)) {
            throw NotFoundException("No product found for productId: $productId")
        }

        val response = mapper.entityToApi(entity).apply {
            serviceAddress = serviceUtil.serviceAddress
        }

        LOG.debug("getProduct: found productId: ${response.productId}")

        return response
    }

    override fun deleteProduct(productId: Int) {
        LOG.debug("deleteProduct: tries to delete an entity with product: $productId")
        repository.findByProductId(productId)?.apply {
            repository.delete(this)
        }
    }
}
