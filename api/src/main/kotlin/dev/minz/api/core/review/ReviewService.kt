package dev.minz.api.core.review

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface ReviewService {
    @PostMapping(
        value = ["/review"],
        consumes = [APPLICATION_JSON],
        produces = [APPLICATION_JSON],
    )
    fun createReview(@RequestBody body: Review): Review

    @GetMapping(
        value = ["/review"],
        produces = [APPLICATION_JSON]
    )
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): List<Review>?

    @DeleteMapping(value = ["/review"])
    fun deleteReviews(@RequestParam(value = "productId", required = true) productId: Int)
}
