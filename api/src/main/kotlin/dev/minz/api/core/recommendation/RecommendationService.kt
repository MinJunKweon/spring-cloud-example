package dev.minz.api.core.recommendation

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

interface RecommendationService {
    @GetMapping(
        value = ["/recommendation"],
        produces = [APPLICATION_JSON]
    )
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): List<Recommendation>?
}
