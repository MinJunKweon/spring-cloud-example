package dev.minz.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Component
class ProductCompositeIntegration(
    private val restTemplate: RestTemplate,
    private val mapper: ObjectMapper,

    @Value("\${app.product-service.host}") productServiceHost: String,
    @Value("\${app.product-service.port}") productServicePort: Int,

    @Value("\${app.recommendation-service.host}") recommendationServiceHost: String,
    @Value("\${app.recommendation-service.port}") recommendationServicePort: Int,

    @Value("\${app.review-service.host}") reviewServiceHost: String,
    @Value("\${app.review-service.port}") reviewServicePort: Int,
) : ProductService, RecommendationService, ReviewService {
    companion object {
        /* sample: SERVICE_URL_FORMAT.format(host, port, serviceName) */
        private const val SERVICE_URL_FORMAT = "http://%s:%d/%s"
    }

    private val productServiceUrl = SERVICE_URL_FORMAT.format(productServiceHost, productServicePort, "product")
    private val recommendationServiceUrl =
        SERVICE_URL_FORMAT.format(recommendationServiceHost, recommendationServicePort, "recommendation")
    private val reviewServiceUrl = SERVICE_URL_FORMAT.format(reviewServiceHost, reviewServicePort, "review")

    override fun getProduct(productId: Int): Product? =
        restTemplate.getForObject("$productServiceUrl/$productId")

    override fun getRecommendations(productId: Int): List<Recommendation>? =
        restTemplate.exchange(
            "$recommendationServiceUrl?productId=$productId",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Recommendation>>() {}
        ).body

    override fun getReviews(productId: Int): List<Review>? =
        restTemplate.exchange(
            "$reviewServiceUrl?productId=$productId",
            HttpMethod.GET,
            null,
            object : ParameterizedTypeReference<List<Review>>() {}
        ).body
}
