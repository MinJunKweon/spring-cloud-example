package dev.minz.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import dev.minz.util.http.HttpErrorInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
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

        private val LOG = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)
    }

    private val productServiceUrl = SERVICE_URL_FORMAT.format(productServiceHost, productServicePort, "product")
    private val recommendationServiceUrl =
        SERVICE_URL_FORMAT.format(recommendationServiceHost, recommendationServicePort, "recommendation")
    private val reviewServiceUrl = SERVICE_URL_FORMAT.format(reviewServiceHost, reviewServicePort, "review")

    override fun createProduct(body: Product): Product =
        runCatchingHttpClientException {
            checkNotNull(restTemplate.postForObject(productServiceUrl, body, Product::class.java)) {
                "Product created failed. product id: ${body.productId}"
            }.also {
                LOG.debug("Created a product with id: ${it.productId}")
            }
        }

    override fun getProduct(productId: Int): Product? =
        runCatchingHttpClientException {
            restTemplate.getForObject<Product>("$productServiceUrl/$productId")
        }

    override fun deleteProduct(productId: Int) =
        runCatchingHttpClientException {
            val url = "$productServiceUrl/$productId"
            LOG.debug("Will call the deleteProduct API on URL: $url")
            restTemplate.delete(url)
        }

    override fun createRecommendation(body: Recommendation): Recommendation =
        runCatchingHttpClientException {
            LOG.debug("Will post a new recommendation to URL: $recommendationServiceUrl")
            checkNotNull(restTemplate.postForObject(recommendationServiceUrl, body, Recommendation::class.java)) {
                "Recommendation created failed. product id: ${body.productId}"
            }.also {
                LOG.debug("Created a recommendation with product id: ${it.productId}")
            }
        }

    override fun getRecommendations(productId: Int): List<Recommendation>? =
        runCatching {
            val url = "$recommendationServiceUrl?productId=$productId"
            LOG.debug("Will call the getRecommendations API on URL: $url")
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<Recommendation>>() {}
            ).body.also {
                LOG.debug("Found ${it?.size} recommendations for a product with id: $productId")
            }
        }.getOrElse {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: ${it.message}")
            emptyList()
        }

    override fun deleteRecommendations(productId: Int) =
        runCatchingHttpClientException {
            val url = "$recommendationServiceUrl?productId=$productId"
            LOG.debug("Will call the deleteRecommendations API on URL: $url")
            restTemplate.delete(url)
        }

    override fun createReview(body: Review): Review =
        runCatchingHttpClientException {
            LOG.debug("Will post a new review to URL: $reviewServiceUrl")

            checkNotNull(restTemplate.postForObject(reviewServiceUrl, body, Review::class.java)) {
                "Review created failed. product id: ${body.productId}"
            }.also {
                LOG.debug("Created a review with product id: ${it.productId}")
            }
        }

    override fun getReviews(productId: Int): List<Review>? =
        runCatching {
            val url = "$reviewServiceUrl?productId=$productId"
            LOG.debug("Will call the getReviews API on URL: $url")
            restTemplate.exchange(
                "$reviewServiceUrl?productId=$productId",
                HttpMethod.GET,
                null,
                object : ParameterizedTypeReference<List<Review>>() {}
            ).body.also {
                LOG.debug("Found ${it?.size} reviews for a product with id: $productId")
            }
        }.getOrElse {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: ${it.message}")
            emptyList()
        }

    override fun deleteReviews(productId: Int) =
        runCatchingHttpClientException {
            val url = "$reviewServiceUrl?productId=$productId"
            LOG.debug("Will call the deleteReviews API on URL: $url")
            restTemplate.delete(url)
        }

    private fun HttpClientErrorException.getErrorMessage(): String {
        return runCatching {
            mapper.readValue(responseBodyAsString, HttpErrorInfo::class.java).message
        }.getOrNull() ?: "${this.message}"
    }

    private fun <R> runCatchingHttpClientException(block: () -> R) =
        try {
            block()
        } catch (ex: HttpClientErrorException) {
            when (ex.statusCode) {
                HttpStatus.NOT_FOUND -> throw NotFoundException(ex.getErrorMessage())
                HttpStatus.UNPROCESSABLE_ENTITY -> throw InvalidInputException(ex.getErrorMessage())
                else -> {
                    LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.statusCode)
                    LOG.warn("Error body: {}", ex.responseBodyAsString)
                    throw ex
                }
            }
        }
}
