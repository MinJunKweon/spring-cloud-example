package dev.minz.microservices.composite.product.services

import com.fasterxml.jackson.databind.ObjectMapper
import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.api.event.Event
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import dev.minz.util.http.HttpErrorInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.function.StreamBridge
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ProductCompositeIntegration(
    private val streamBridge: StreamBridge,
    @Qualifier("loadBalancedWebClientBuilder")
    webClientBuilder: WebClient.Builder,
    private val mapper: ObjectMapper,
) : ProductService, RecommendationService, ReviewService {
    companion object {
        private val LOG = LoggerFactory.getLogger(ProductCompositeIntegration::class.java)
    }

    private val webClient = webClientBuilder.build()

    private val productServiceUrl = "http://product"
    private val recommendationServiceUrl = "http://recommendation"
    private val reviewServiceUrl = "http://review"

    override fun createProduct(body: Product): Product {
        streamBridge.send("products", Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    override fun getProduct(productId: Int): Mono<Product> {
        val url = "$productServiceUrl/product/$productId"
        LOG.debug("Will call the getProduct API on URL: $url")
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono<Product>()
            .log()
            .onErrorMap(WebClientResponseException::class.java) { convertException(it) }
    }

    override fun deleteProduct(productId: Int) {
        streamBridge.send("products", Event(Event.Type.DELETE, productId, null))
    }

    override fun createRecommendation(body: Recommendation): Recommendation {
        streamBridge.send("recommendations", Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        val url = "$recommendationServiceUrl/recommendation?productId=$productId"
        LOG.debug("Will call the getRecommendations API on URL: $url")
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToFlux<Recommendation>()
            .log()
            .onErrorMap(WebClientResponseException::class.java) { convertException(it) }
    }

    override fun deleteRecommendations(productId: Int) {
        streamBridge.send("recommendations", Event(Event.Type.DELETE, productId, null))
    }

    override fun createReview(body: Review): Review {
        streamBridge.send("reviews", Event(Event.Type.CREATE, body.productId, body))
        return body
    }

    override fun getReviews(productId: Int): Flux<Review> {
        val url = "$reviewServiceUrl/review?productId=$productId"
        LOG.debug("Will call the getReviews API on URL: $url")
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToFlux<Review>()
            .log()
            .onErrorMap(WebClientResponseException::class.java) { convertException(it) }
    }

    override fun deleteReviews(productId: Int) {
        streamBridge.send("reviews", Event(Event.Type.DELETE, productId, null))
    }

    private fun WebClientResponseException.getErrorMessage(): String {
        return runCatching {
            mapper.readValue(responseBodyAsString, HttpErrorInfo::class.java).message
        }.getOrNull() ?: "${this.message}"
    }

    private fun convertException(ex: WebClientResponseException): Throwable {
        val message = ex.getErrorMessage()
        return when (ex.statusCode) {
            HttpStatus.NOT_FOUND -> NotFoundException(message)
            HttpStatus.UNPROCESSABLE_ENTITY -> InvalidInputException(message)
            else -> ex.also {
                LOG.warn("Got a unexpected HTTP error: ${ex.statusCode}, will rethrow it")
                LOG.warn("Error body: ${ex.responseBodyAsString}")
            }
        }
    }
}
