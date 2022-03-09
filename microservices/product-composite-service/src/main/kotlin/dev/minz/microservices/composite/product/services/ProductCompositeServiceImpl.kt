package dev.minz.microservices.composite.product.services

import dev.minz.api.composite.product.ProductAggregate
import dev.minz.api.composite.product.ProductCompositeService
import dev.minz.api.composite.product.RecommendationSummary
import dev.minz.api.composite.product.ReviewSummary
import dev.minz.api.composite.product.ServiceAddresses
import dev.minz.api.core.product.Product
import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.review.Review
import dev.minz.util.http.ServiceUtil
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeServiceImpl(
    val serviceUtil: ServiceUtil,
    val integration: ProductCompositeIntegration,
) : ProductCompositeService {
    override fun getProduct(productId: Int): ProductAggregate {
        val product = requireNotNull(integration.getProduct(productId)) { "No product found for productId: $productId" }
        val recommendations = integration.getRecommendations(productId)
        val reviews = integration.getReviews(productId)

        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
    }

    private fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>?,
        reviews: List<Review>?,
        serviceAddress: String
    ): ProductAggregate {
        val recommendationSummaries =
            recommendations?.map { RecommendationSummary(it.recommendationId, it.author, it.rate) }
        val reviewSummaries = reviews?.map { ReviewSummary(it.reviewId, it.author, it.subject) }

        val productAddress = product.serviceAddress
        val reviewAddress = reviews?.firstNotNullOfOrNull { it.serviceAddress } ?: ""
        val recommendationAddress = recommendations?.firstNotNullOfOrNull { it.serviceAddress } ?: ""
        val serviceAddresses = ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress)

        return ProductAggregate(
            productId = product.productId,
            name = product.name,
            weight = product.weight,
            recommendations = recommendationSummaries,
            reviews = reviewSummaries,
            serviceAddresses = serviceAddresses,
        )
    }
}
