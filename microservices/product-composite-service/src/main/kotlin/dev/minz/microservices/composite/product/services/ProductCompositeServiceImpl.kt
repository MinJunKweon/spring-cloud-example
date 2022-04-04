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
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductCompositeServiceImpl(
    val serviceUtil: ServiceUtil,
    val integration: ProductCompositeIntegration,
) : ProductCompositeService {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductCompositeServiceImpl::class.java)
    }

    override fun createCompositeProduct(body: ProductAggregate) {
        LOG.debug("createCompositeProduct: creates a new composite entity for productId: ${body.productId}")

        val product = Product(body.productId, body.name, body.weight, null)
        integration.createProduct(product)

        body.recommendations?.forEach {
            val recommendation =
                Recommendation(body.productId, it.recommendationId, it.author, it.rate, it.content, null)
            integration.createRecommendation(recommendation)
        }
        body.reviews?.forEach {
            val review = Review(body.productId, it.reviewId, it.author, it.subject, it.content, null)
            integration.createReview(review)
        }

        LOG.debug("createCompositeProduct: composite entities created for productId: ${body.productId}")
    }

    override fun getCompositeProduct(productId: Int): ProductAggregate {
        LOG.debug("getCompositeProduct: lookup a product aggregate for productId: $productId")
        val product = requireNotNull(integration.getProduct(productId)) { "No product found for productId: $productId" }
        val recommendations = integration.getRecommendations(productId)
        val reviews = integration.getReviews(productId)
        LOG.debug("getCompositeProduct: aggregate entity found for productId: $productId")
        return createProductAggregate(product, recommendations, reviews, serviceUtil.serviceAddress)
    }

    override fun deleteCompositeProduct(productId: Int) {
        LOG.debug("deleteCompositeProduct: Deletes a product aggregate for productId: $productId")
        integration.deleteProduct(productId)
        integration.deleteRecommendations(productId)
        integration.deleteReviews(productId)
        LOG.debug("getCompositeProduct: aggregate entities deleted for productId: $productId")
    }

    private fun createProductAggregate(
        product: Product,
        recommendations: List<Recommendation>?,
        reviews: List<Review>?,
        serviceAddress: String,
    ): ProductAggregate {
        val recommendationSummaries =
            recommendations?.map { RecommendationSummary(it.recommendationId, it.author, it.rate, it.content) }
        val reviewSummaries = reviews?.map { ReviewSummary(it.reviewId, it.author, it.subject, it.content) }

        val productAddress = product.serviceAddress ?: ""
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
