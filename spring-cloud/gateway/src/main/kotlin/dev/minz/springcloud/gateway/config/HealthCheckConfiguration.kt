package dev.minz.springcloud.gateway.config

import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.ReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Configuration
class HealthCheckConfiguration(
    webClientBuilder: WebClient.Builder,
) {
    companion object {
        private val log = LoggerFactory.getLogger(HealthCheckConfiguration::class.java)

        private const val URL_PRODUCT = "http://product"
        private const val URL_RECOMMENDATION = "http://recommendation"
        private const val URL_REVIEW = "http://review"
        private const val URL_PRODUCT_COMPOSITE = "http://product-composite"
    }

    private val webClient = webClientBuilder.build()

    @Bean
    fun healthCheckMicroservices(): ReactiveHealthContributor {
        val indicators = mapOf(
            "product" to ReactiveHealthIndicator { getHealth(URL_PRODUCT) },
            "recommendation" to ReactiveHealthIndicator { getHealth(URL_RECOMMENDATION) },
            "review" to ReactiveHealthIndicator { getHealth(URL_REVIEW) },
            "product-composite" to ReactiveHealthIndicator { getHealth(URL_PRODUCT_COMPOSITE) },
        )
        return CompositeReactiveHealthContributor.fromMap(indicators)
    }

    private fun getHealth(url: String): Mono<Health> {
        val healthCheckUrl = "$url/actuator/health"
        log.debug("Will call the Health API on URL: $healthCheckUrl")
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono<Health>()
            .map { Health.Builder().up().build() }
            .onErrorResume { Mono.just(Health.Builder().down().build()) }
            .log()
    }
}
