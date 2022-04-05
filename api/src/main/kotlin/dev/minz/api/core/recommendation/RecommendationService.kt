package dev.minz.api.core.recommendation

import dev.minz.util.constant.APPLICATION_JSON
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import reactor.core.publisher.Flux

interface RecommendationService {

    fun createRecommendation(@RequestBody body: Recommendation): Recommendation

    @GetMapping(
        value = ["/recommendation"],
        produces = [APPLICATION_JSON]
    )
    fun getRecommendations(@RequestParam(value = "productId", required = true) productId: Int): Flux<Recommendation>

    fun deleteRecommendations(@RequestParam(value = "productId", required = true) productId: Int)
}
