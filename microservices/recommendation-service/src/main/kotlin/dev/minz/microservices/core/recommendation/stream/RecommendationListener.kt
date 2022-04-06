package dev.minz.microservices.core.recommendation.stream

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.api.event.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RecommendationListener(
    private val recommendationService: RecommendationService,
) {

    @Bean
    fun process(): (Event<Int, Recommendation>) -> Unit = {
        when (it.eventType) {
            Event.Type.CREATE -> recommendationService.createRecommendation(it.data)
            Event.Type.DELETE -> recommendationService.deleteRecommendations(it.key)
        }
    }
}
