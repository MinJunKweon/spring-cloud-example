package dev.minz.microservices.core.review.services

import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.microservices.core.review.persistence.ReviewRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.scheduler.Scheduler
import java.util.logging.Level

@RestController
class ReviewServiceImpl(
    private val repository: ReviewRepository,
    private val mapper: ReviewMapper,
    private val serviceUtil: ServiceUtil,
    private val threadPoolScheduler: Scheduler,
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

    override fun getReviews(productId: Int): Flux<Review> {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        LOG.info("Will get reviews for product with id=$productId")

        return asyncFlux { Flux.fromIterable(getByProductId(productId)) }.log(null, Level.FINE)
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

    private fun <T> asyncFlux(supplier: () -> Publisher<T>): Flux<T> =
        Flux.defer(supplier).subscribeOn(threadPoolScheduler)
}
