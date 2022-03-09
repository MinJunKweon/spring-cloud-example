package dev.minz.microservices.core.recommendation

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecommendationServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 113
        private const val PRODUCT_ID_NEGATIVE = -1
    }

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `제품 아이디에 따른 추천 검색 검증`() {
        client.get()
            .uri("/recommendation?productId=$PRODUCT_ID_OK")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.length()").isEqualTo(3)
            .jsonPath("\$[0].productId").isEqualTo(PRODUCT_ID_OK)
    }

    @Test
    fun `파라미터 없는 경우 검증`() {
        client.get()
            .uri("/recommendation")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/recommendation")
            .jsonPath("\$.message").isEqualTo("Required int parameter 'productId' is not present")
    }

    @Test
    fun `추천 없는 경우 검증`() {
        client.get()
            .uri("/recommendation?productId=$PRODUCT_ID_NOT_FOUND")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.length()").isEqualTo(0)
    }

    @Test
    fun `제품 아이디 음수인 경우 검증`() {
        client.get()
            .uri("/recommendation?productId=$PRODUCT_ID_NEGATIVE")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/recommendation")
            .jsonPath("\$.message").isEqualTo("Invalid productId: $PRODUCT_ID_NEGATIVE")
    }
}
