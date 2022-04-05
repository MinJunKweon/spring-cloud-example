package dev.minz.microservices.core.review.stream

import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.api.event.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message

@Configuration
class ReviewListener(
    private val reviewService: ReviewService,
) {
    @Bean
    fun process(): (Message<Event<Int, Review>>) -> Unit = {
        val payload = it.payload
        when (payload.eventType) {
            Event.Type.CREATE -> reviewService.createReview(payload.data)
            Event.Type.DELETE -> reviewService.deleteReviews(payload.key)
        }
    }
}
