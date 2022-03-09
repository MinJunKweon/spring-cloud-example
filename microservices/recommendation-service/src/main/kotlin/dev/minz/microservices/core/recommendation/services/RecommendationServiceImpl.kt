package dev.minz.microservices.core.recommendation.services

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class RecommendationServiceImpl(
    private val serviceUtil: ServiceUtil
) : RecommendationService {
    companion object {
        private val LOG = LoggerFactory.getLogger(RecommendationServiceImpl::class.java)
    }

    override fun getRecommendations(productId: Int): List<Recommendation>? {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        if (productId == 113) {
            LOG.debug("No recommendations found for product id: $productId")
            return arrayListOf()
        }

        return arrayListOf(
            Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.serviceAddress),
            Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.serviceAddress),
            Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.serviceAddress),
        ).also {
            LOG.debug("/recommendation response size: ${it.size}")
        }
    }
}
