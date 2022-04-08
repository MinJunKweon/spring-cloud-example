package dev.minz.microservices.core.recommendation

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import dev.minz.util.exceptions.InvalidInputException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RecommendationServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var recommendationService: RecommendationService

    @Autowired
    private lateinit var repository: RecommendationRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()
    }

    @Test
    fun `제품 아이디에 따른 추천 검색 검증`() {
        recommendationService.createRecommendation(createRecommendation(1, 1))
        recommendationService.createRecommendation(createRecommendation(1, 2))
        recommendationService.createRecommendation(createRecommendation(1, 3))
        Assertions.assertEquals(3, repository.findByProductId(1).count().block())

        getAndVerifyRecommendationsByProductId(1, HttpStatus.OK)
            .jsonPath("\$.length()").isEqualTo(3)
            .jsonPath("\$[2].productId").isEqualTo(1)
            .jsonPath("\$[2].recommendationId").isEqualTo(3)
    }

    @Test
    fun `중복 엔티티 검증`() {
        val recommendation = createRecommendation(1, 1)
        recommendationService.createRecommendation(recommendation)
        Assertions.assertEquals(1, repository.count().block())

        assertThrows<InvalidInputException> {
            recommendationService.createRecommendation(recommendation)
        }

        Assertions.assertEquals(1, repository.count().block())
    }

    @Test
    fun `추천 삭제 검증`() {
        val recommendation = createRecommendation(1, 1)
        recommendationService.createRecommendation(recommendation)
        Assertions.assertEquals(1, repository.findByProductId(1).count().block())

        recommendationService.deleteRecommendations(1)
        Assertions.assertEquals(0, repository.findByProductId(1).count().block())

        recommendationService.deleteRecommendations(1)
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
        getAndVerifyRecommendationsByProductId("?productId=-1", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/recommendation")
            .jsonPath("\$.message").isEqualTo("Invalid productId: -1")
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

    private fun createRecommendation(
        productId: Int,
        recommendationId: Int,
    ) = Recommendation(
        productId, recommendationId,
        "Author $recommendationId", recommendationId, "Content $recommendationId", "SA"
    )
}
