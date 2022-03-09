package dev.minz.api.core.review

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

interface ReviewService {
    @GetMapping(
        value = ["/review"],
        produces = [APPLICATION_JSON]
    )
    fun getReviews(@RequestParam(value = "productId", required = true) productId: Int): List<Review>?
}
