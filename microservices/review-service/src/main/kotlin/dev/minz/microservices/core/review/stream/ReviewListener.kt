package dev.minz.microservices.core.review.stream

import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.api.event.Event
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ReviewListener(
    private val reviewService: ReviewService,
) {
    @Bean
    fun process(): (Event<Int, Review>) -> Unit = {
        when (it.eventType) {
            Event.Type.CREATE -> reviewService.createReview(it.data)
            Event.Type.DELETE -> reviewService.deleteReviews(it.key)
        }
    }
}
