package dev.minz.microservices.composite.product.services

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Service
class HealthIndicateService(
    webClientBuilder: WebClient.Builder,

    @Value("\${app.product-service.host}") productServiceHost: String,
    @Value("\${app.product-service.port}") productServicePort: Int,

    @Value("\${app.recommendation-service.host}") recommendationServiceHost: String,
    @Value("\${app.recommendation-service.port}") recommendationServicePort: Int,

    @Value("\${app.review-service.host}") reviewServiceHost: String,
    @Value("\${app.review-service.port}") reviewServicePort: Int,
) {
    companion object {
        /* sample: SERVICE_URL_FORMAT.format(host, port, serviceName) */
        private const val SERVICE_URL_FORMAT = "http://%s:%d/%s"

        private val LOG = LoggerFactory.getLogger(HealthIndicateService::class.java)
    }

    private val webClient = webClientBuilder.build()

    private val productServiceUrl = SERVICE_URL_FORMAT.format(productServiceHost, productServicePort, "product")
    private val recommendationServiceUrl =
        SERVICE_URL_FORMAT.format(recommendationServiceHost, recommendationServicePort, "recommendation")
    private val reviewServiceUrl = SERVICE_URL_FORMAT.format(reviewServiceHost, reviewServicePort, "review")

    fun getProductHealth(): Mono<Health> = getHealth(productServiceUrl)
    fun getRecommendationHealth(): Mono<Health> = getHealth(recommendationServiceUrl)
    fun getReviewHealth(): Mono<Health> = getHealth(reviewServiceUrl)

    private fun getHealth(url: String): Mono<Health> {
        val healthCheckUrl = "$url/actuator/health"
        LOG.debug("Will call the Health API on URL: $healthCheckUrl")
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono<String>()
            .map { Health.Builder().up().build() }
            .onErrorResume { Mono.just(Health.Builder().down(it).build()) }
            .log()
    }
}
