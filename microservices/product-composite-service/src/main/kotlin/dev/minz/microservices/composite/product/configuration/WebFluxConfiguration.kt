package dev.minz.microservices.composite.product.configuration

import dev.minz.microservices.composite.product.services.HealthIndicateService
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthContributor
import org.springframework.boot.actuate.health.ReactiveHealthIndicator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebFluxConfiguration {

    @Bean
    fun webClientBuilder(): WebClient.Builder =
        WebClient.builder()

    @Bean
    fun coreServices(
        healthIndicator: HealthIndicateService,
    ): ReactiveHealthContributor {
        val indicators = mapOf(
            "product" to ReactiveHealthIndicator { healthIndicator.getProductHealth() },
            "recommendation" to ReactiveHealthIndicator { healthIndicator.getRecommendationHealth() },
            "review" to ReactiveHealthIndicator { healthIndicator.getReviewHealth() }
        )
        return CompositeReactiveHealthContributor.fromMap(indicators)
    }
}
