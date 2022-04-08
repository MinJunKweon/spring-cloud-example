package dev.minz.microservices.core.review

import dev.minz.api.core.review.Review
import dev.minz.api.core.review.ReviewService
import dev.minz.microservices.core.review.persistence.ReviewRepository
import dev.minz.util.exceptions.InvalidInputException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec

@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = ["spring.datasource.url=jdbc:h2:mem:review-db", "eureka.client.enabled=false"]
)
class ReviewServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var reviewService: ReviewService

    @Autowired
    private lateinit var repository: ReviewRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun `제품 아이디로 리뷰 검색 검증`() {
        Assertions.assertEquals(0, repository.findByProductId(1).size)

        reviewService.createReview(createReview(1, 1))
        reviewService.createReview(createReview(1, 2))
        reviewService.createReview(createReview(1, 3))

        Assertions.assertEquals(3, repository.findByProductId(1).size)

        getAndVerifyReviewsByProductId(1, HttpStatus.OK)
            .jsonPath("\$.length()").isEqualTo(3)
            .jsonPath("\$[2].productId").isEqualTo(1)
            .jsonPath("\$[2].reviewId").isEqualTo(3)
    }

    @Test
    fun `중복 삽입 검증`() {
        val review = createReview(1, 1)
        Assertions.assertEquals(0, repository.count())
        reviewService.createReview(review)
        Assertions.assertEquals(1, repository.count())

        assertThrows<InvalidInputException> {
            reviewService.createReview(review)
        }

        Assertions.assertEquals(1, repository.count())
    }

    @Test
    fun `리뷰 삭제 검증`() {
        val review = createReview(1, 1)
        reviewService.createReview(review)
        Assertions.assertEquals(1, repository.findByProductId(1).size)

        reviewService.deleteReviews(1)
        Assertions.assertEquals(0, repository.findByProductId(1).size)

        reviewService.deleteReviews(1)
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
        getAndVerifyReviewsByProductId("?productId=-1", HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Invalid productId: -1")
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

    private fun createReview(productId: Int, reviewId: Int) =
        Review(
            productId, reviewId,
            "Author $reviewId", "Subject $reviewId", "Content $reviewId", "SA"
        )
}
