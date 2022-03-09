package dev.minz.microservices.core.review.services

import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ReviewServiceImpl(
    private val serviceUtil: ServiceUtil
) : ReviewService {
    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewServiceImpl::class.java)
    }

    override fun getReviews(productId: Int): List<Review>? {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }
        if (productId == 213) {
            LOG.debug("No reviews found for productId: $productId")
            return arrayListOf()
        }

        return arrayListOf(
            Review(productId, 1, "Author 1", "Subject 1", "Content 1", serviceUtil.serviceAddress),
            Review(productId, 2, "Author 2", "Subject 2", "Content 2", serviceUtil.serviceAddress),
            Review(productId, 3, "Author 3", "Subject 3", "Content 3", serviceUtil.serviceAddress),
        ).also {
            LOG.debug("/reviews response size: ${it.size}")
        }
    }
}
