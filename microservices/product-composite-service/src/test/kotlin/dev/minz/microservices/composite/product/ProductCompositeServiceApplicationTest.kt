package dev.minz.microservices.composite.product

import dev.minz.api.core.product.Product
import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.review.Review
import dev.minz.microservices.composite.product.services.ProductCompositeIntegration
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.exceptions.NotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

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

    @MockBean
    private lateinit var compositeIntegration: ProductCompositeIntegration

    @BeforeEach
    fun setUp() {
        val mockRecommendation = Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-address")
        val mockReview = Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-address")

        `when`(compositeIntegration.getProduct(PRODUCT_ID_OK))
            .thenReturn(Product(PRODUCT_ID_OK, "name", 1, "mock-address"))
        `when`(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
            .thenReturn(listOf(mockRecommendation))
        `when`(compositeIntegration.getReviews(PRODUCT_ID_OK))
            .thenReturn(listOf(mockReview))

        `when`(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
            .thenThrow(NotFoundException("NOT FOUND: $PRODUCT_ID_NOT_FOUND"))
        `when`(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
            .thenThrow(InvalidInputException("INVALID: $PRODUCT_ID_INVALID"))
    }

    @Test
    fun `제품 ID 검색 검증`() {
        client.get()
            .uri("/product-composite/$PRODUCT_ID_OK")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.productId").isEqualTo(PRODUCT_ID_OK)
            .jsonPath("\$.recommendations.length()").isEqualTo(1)
            .jsonPath("\$.reviews.length()").isEqualTo(1)
    }

    @Test
    fun `제품 없는 아이디 검색 검증`() {
        client.get()
            .uri("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/product-composite/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("\$.message").isEqualTo("NOT FOUND: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun `잘못된 제품 아이디 입수 검증`() {
        client.get()
            .uri("/product-composite/$PRODUCT_ID_INVALID")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/product-composite/$PRODUCT_ID_INVALID")
            .jsonPath("\$.message").isEqualTo("INVALID: $PRODUCT_ID_INVALID")
    }
}
