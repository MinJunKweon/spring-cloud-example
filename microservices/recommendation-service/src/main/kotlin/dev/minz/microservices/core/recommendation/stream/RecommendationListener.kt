package dev.minz.microservices.core.recommendation.stream

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.api.event.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message

@Configuration
class RecommendationListener(
    private val recommendationService: RecommendationService,
) {

    @Bean
    fun process(): (Message<Event<Int, Recommendation>>) -> Unit = {
        val payload = it.payload
        when (payload.eventType) {
            Event.Type.CREATE -> recommendationService.createRecommendation(payload.data)
            Event.Type.DELETE -> recommendationService.deleteRecommendations(payload.key)
        }
    }
}
