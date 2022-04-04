package dev.minz.microservices.core.recommendation

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 113
        private const val PRODUCT_ID_NEGATIVE = -1
    }

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: RecommendationRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun `제품 아이디에 따른 추천 검색 검증`() {
        postAndVerifyRecommendation(PRODUCT_ID_OK, 1, HttpStatus.OK)
        postAndVerifyRecommendation(PRODUCT_ID_OK, 2, HttpStatus.OK)
        postAndVerifyRecommendation(PRODUCT_ID_OK, 3, HttpStatus.OK)

        Assertions.assertEquals(3, repository.findByProductId(PRODUCT_ID_OK).size)

        getAndVerifyRecommendationsByProductId(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("\$.length()").isEqualTo(3)
            .jsonPath("\$[2].productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("\$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun `중복 엔티티 검증`() {
        val recommendationId = 1
        postAndVerifyRecommendation(PRODUCT_ID_OK, recommendationId, HttpStatus.OK)
            .jsonPath("\$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("\$.recommendationId").isEqualTo(recommendationId)

        Assertions.assertEquals(1, repository.count())

        postAndVerifyRecommendation(PRODUCT_ID_OK, recommendationId, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/recommendation")
            .jsonPath("\$.message")
            .isEqualTo("Duplicate key, product Id: $PRODUCT_ID_OK, recommendation Id : $recommendationId")

        Assertions.assertEquals(1, repository.count())
    }

    @Test
    fun `추천 삭제 검증`() {
        val recommendationId = 1
        postAndVerifyRecommendation(PRODUCT_ID_OK, recommendationId, HttpStatus.OK)
        Assertions.assertEquals(1, repository.findByProductId(PRODUCT_ID_OK).size)

        deleteAndVerifyRecommendationsByProductId(PRODUCT_ID_OK, HttpStatus.OK)
        Assertions.assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size)

        deleteAndVerifyRecommendationsByProductId(PRODUCT_ID_OK, HttpStatus.OK)
    }

    @Test
    fun `파라미터 없는 경우 검증`() {
        getAndVerifyRecommendationsByProductId("", HttpStatus.BAD_REQUEST)
            .jsonPath("\$.path").isEqualTo("/recommendation")
            .jsonPath("\$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun `파라미터 잘못된 경우 검증`() {
        getAndVerifyRecommendationsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("$.path").isEqualTo("/recommendation")
            .jsonPath("$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun `추천 없는 경우 검증`() {
        getAndVerifyRecommendationsByProductId("?productId=113", HttpStatus.OK)
            .jsonPath("\$.length()").isEqualTo(0)
    }

    @Test
    fun `제품 아이디 음수인 경우 검증`() {
        getAndVerifyRecommendationsByProductId("?productId=$PRODUCT_ID_NEGATIVE", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/recommendation")
            .jsonPath("\$.message").isEqualTo("Invalid productId: $PRODUCT_ID_NEGATIVE")
    }

    private fun getAndVerifyRecommendationsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyRecommendationsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyRecommendationsByProductId(
        productIdQuery: String,
        expectedStatus: HttpStatus,
    ): BodyContentSpec {
        return client.get()
            .uri("/recommendation$productIdQuery")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyRecommendation(
        productId: Int,
        recommendationId: Int,
        expectedStatus: HttpStatus,
    ): BodyContentSpec {
        val recommendation = Recommendation(
            productId, recommendationId,
            "Author $recommendationId", recommendationId, "Content $recommendationId", "SA"
        )
        return client.post()
            .uri("/recommendation")
            .body(Mono.just(recommendation), Recommendation::class.java)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyRecommendationsByProductId(
        productId: Int,
        expectedStatus: HttpStatus,
    ): BodyContentSpec {
        return client.delete()
            .uri("/recommendation?productId=$productId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}
