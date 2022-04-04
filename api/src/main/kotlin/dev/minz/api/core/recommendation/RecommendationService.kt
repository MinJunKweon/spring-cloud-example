package dev.minz.api.core.recommendation

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

interface RecommendationService {
    @PostMapping(
        value = ["/recommendation"],
        consumes = [APPLICATION_JSON],
        produces = [APPLICATION_JSON],
    )
    fun createRecommendation(@RequestBody body: Recommendation): Recommendation

    @GetMapping(
        value = ["/recommendation"],
        produces = [APPLICATION_JSON]
    )
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): List<Recommendation>?

    @DeleteMapping(value = ["/recommendation"])
    fun deleteRecommendations(@RequestParam(value = "productId", required = true) productId: Int)
}
