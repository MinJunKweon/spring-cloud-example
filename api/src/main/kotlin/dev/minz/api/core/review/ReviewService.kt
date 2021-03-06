package dev.minz.api.core.review

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface ReviewService {

    fun createReview(@RequestBody body: Review): Review

    @GetMapping(
        value = ["/review"],
        produces = [APPLICATION_JSON]
    )
    suspend fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): List<Review>

    fun deleteReviews(@RequestParam(value = "productId", required = true) productId: Int)
}
