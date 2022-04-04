package dev.minz.microservices.core.review

import dev.minz.api.core.review.Review
import dev.minz.microservices.core.review.persistence.ReviewRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = ["spring.datasource.url=jdbc:h2:mem:review-db"]
)
class ReviewServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 213
        private const val PRODUCT_ID_NEGATIVE = -1
    }

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ReviewRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun `제품 아이디로 리뷰 검색 검증`() {
        Assertions.assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size)

        postAndVerifyReview(PRODUCT_ID_OK, 1, HttpStatus.OK)
        postAndVerifyReview(PRODUCT_ID_OK, 2, HttpStatus.OK)
        postAndVerifyReview(PRODUCT_ID_OK, 3, HttpStatus.OK)

        Assertions.assertEquals(3, repository.findByProductId(PRODUCT_ID_OK).size)

        getAndVerifyReviewsByProductId(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("\$.length()").isEqualTo(3)
            .jsonPath("\$[2].productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("\$[2].reviewId").isEqualTo(3)
    }

    @Test
    fun `중복 삽입 검증`() {
        val reviewId = 1
        Assertions.assertEquals(0, repository.count())

        postAndVerifyReview(PRODUCT_ID_OK, reviewId, HttpStatus.OK)
            .jsonPath("\$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("\$.reviewId").isEqualTo(reviewId)

        Assertions.assertEquals(1, repository.count())

        postAndVerifyReview(PRODUCT_ID_OK, reviewId, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Duplicate key, product Id: $PRODUCT_ID_OK, review Id: $reviewId")

        Assertions.assertEquals(1, repository.count())
    }

    @Test
    fun `리뷰 삭제 검증`() {
        val reviewId = 1
        postAndVerifyReview(PRODUCT_ID_OK, reviewId, HttpStatus.OK)
        Assertions.assertEquals(1, repository.findByProductId(PRODUCT_ID_OK).size)

        deleteAndVerifyReviewsByProductId(PRODUCT_ID_OK, HttpStatus.OK)
        Assertions.assertEquals(0, repository.findByProductId(PRODUCT_ID_OK).size)

        deleteAndVerifyReviewsByProductId(PRODUCT_ID_OK, HttpStatus.OK)
    }

    @Test
    fun `제품 아이디가 빈 경우 검증`() {
        getAndVerifyReviewsByProductId("", HttpStatus.BAD_REQUEST)
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun `제품 아이디 잘못된 경우 검증`() {
        getAndVerifyReviewsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun `제품 아이디로 리뷰 없는 경우 검증`() {
        getAndVerifyReviewsByProductId("?productId=213", HttpStatus.OK)
            .jsonPath("\$.length()").isEqualTo(0)
    }

    @Test
    fun `제품 아이디 음수 검증`() {
        getAndVerifyReviewsByProductId("?productId=$PRODUCT_ID_NEGATIVE", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Invalid productId: $PRODUCT_ID_NEGATIVE")
    }

    private fun getAndVerifyReviewsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyReviewsByProductId("?productId=$productId", expectedStatus)
    }

    private fun getAndVerifyReviewsByProductId(productIdQuery: String, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
            .uri("/review$productIdQuery")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyReview(productId: Int, reviewId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        val review = Review(
            productId, reviewId,
            "Author $reviewId", "Subject $reviewId", "Content $reviewId", "SA"
        )
        return client.post()
            .uri("/review")
            .body(Mono.just(review), Review::class.java)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyReviewsByProductId(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.delete()
            .uri("/review?productId=$productId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}
