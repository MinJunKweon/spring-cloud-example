package dev.minz.microservices.composite.product

import com.ninjasquad.springmockk.MockkBean
import dev.minz.api.composite.product.ProductAggregate
import dev.minz.api.composite.product.RecommendationSummary
import dev.minz.api.composite.product.ReviewSummary
import dev.minz.api.core.product.Product
import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.review.Review
import dev.minz.microservices.composite.product.services.ProductCompositeIntegration
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.justRun
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class ProductCompositeServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 2
        private const val PRODUCT_ID_INVALID = 3
    }

    @Autowired
    private lateinit var client: WebTestClient

    @MockkBean
    private lateinit var compositeIntegration: ProductCompositeIntegration

    @BeforeEach
    fun setUp() {
        val mockProduct = Product(PRODUCT_ID_OK, "name", 1, "mock-address")
        val mockRecommendation = Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address")
        val mockReview = Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address")

        coEvery { compositeIntegration.getProduct(PRODUCT_ID_OK) } returns mockProduct
        coEvery { compositeIntegration.getRecommendations(PRODUCT_ID_OK) } returns listOf(mockRecommendation)
        coEvery { compositeIntegration.getReviews(PRODUCT_ID_OK) } returns listOf(mockReview)

        coEvery { compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND) } throws
            NotFoundException("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
        coEvery { compositeIntegration.getProduct(PRODUCT_ID_INVALID) } throws
            InvalidInputException("INVALID: $PRODUCT_ID_INVALID")

        val product = Product(1, "name", 1, null)
        val recommendation = Recommendation(1, 1, "a", 1, "c", null)
        val review = Review(1, 1, "a", "s", "c", null)
        every { compositeIntegration.createProduct(product) } returns product
        every { compositeIntegration.createRecommendation(recommendation) } returns recommendation
        every { compositeIntegration.createReview(review) } returns review

        justRun { compositeIntegration.deleteProduct(1) }
        justRun { compositeIntegration.deleteRecommendations(1) }
        justRun { compositeIntegration.deleteReviews(1) }
    }

    @Test
    fun `추천, 리뷰 없는 제품 생성 검증`() {
        val compositeProduct = ProductAggregate(1, "name", 1, null, null, null)
        postAndVerifyProduct(compositeProduct, HttpStatus.OK)
    }

    @Test
    fun `제품 생성 검증`() {
        val compositeProduct = ProductAggregate(
            1,
            "name",
            1,
            listOf(RecommendationSummary(1, "a", 1, "c")),
            listOf(ReviewSummary(1, "a", "s", "c")),
            null,
        )
        postAndVerifyProduct(compositeProduct, HttpStatus.OK)
    }

    @Test
    fun `제품 삭제 검증`() {
        val compositeProduct = ProductAggregate(
            1,
            "name",
            1,
            listOf(RecommendationSummary(1, "a", 1, "c")),
            listOf(ReviewSummary(1, "a", "s", "c")),
            null,
        )
        postAndVerifyProduct(compositeProduct, HttpStatus.OK)

        deleteAndVerifyProduct(compositeProduct.productId, HttpStatus.OK)
        deleteAndVerifyProduct(compositeProduct.productId, HttpStatus.OK)
    }

    @Test
    fun `제품 ID 검색 검증`() {
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("\$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("\$.recommendations.length()").isEqualTo(1)
            .jsonPath("\$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun `제품 없는 아이디 검색 검증`() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
            .jsonPath("\$.path").isEqualTo("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("\$.message").isEqualTo("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun `잘못된 제품 아이디 입수 검증`() {
        getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/product-composite/$PRODUCT_ID_INVALID")
            .jsonPath("\$.message").isEqualTo("INVALID: $PRODUCT_ID_INVALID")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
            .uri("/product-composite/$productId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyProduct(compositeProduct: ProductAggregate, expectedStatus: HttpStatus) {
        client.post()
            .uri("/product-composite")
            .body(Mono.just<Any>(compositeProduct), ProductAggregate::class.java)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus) {
        client.delete()
            .uri("/product-composite/$productId")
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
    }
}
