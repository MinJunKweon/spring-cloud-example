package dev.minz.microservices.core.review.services

import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.microservices.core.review.persistence.ReviewRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewServiceImpl(
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil,
) : ReviewService {
    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewServiceImpl::class.java)
    }

    override fun createReview(body: Review): Review {
        require(body.productId > 0) { throw InvalidInputException("Invalid productId: ${body.productId}") }

        val entity = mapper.apiToEntity(body)
        val newEntity = try {
            repository.save(entity)
        } catch (dive: DataIntegrityViolationException) {
            throw InvalidInputException("Duplicate key, product Id: ${body.productId}, review Id: ${body.reviewId}")
        }

        LOG.debug("createReview: created a review entity: ${body.productId}/${body.reviewId}")
        return mapper.entityToApi(newEntity)
    }

    override suspend fun getReviews(productId: Int): List<Review> {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        LOG.info("Will get reviews for product with id=$productId")

        return getByProductId(productId).asFlow().toList()
    }

    protected fun getByProductId(productId: Int): List<Review> {
        val entityList = repository.findByProductId(productId)
        val list = mapper.entityListToApiList(entityList).map { review ->
            review.apply { serviceAddress = serviceUtil.serviceAddress }
        }
        LOG.debug("getReviews: response size: ${list.size}")
        return list
    }

    override fun deleteReviews(productId: Int) {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        LOG.debug("deleteReviews: tries to delete reviews for the productId: $productId")
        repository.deleteAll(repository.findByProductId(productId))
    }
}
