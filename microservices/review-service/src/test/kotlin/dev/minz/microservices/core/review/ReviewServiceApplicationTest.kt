package dev.minz.microservices.core.review

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ReviewServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 213
        private const val PRODUCT_ID_NEGATIVE = -1
    }

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `제품 아이디로 리뷰 검색 검증`() {
        client.get()
            .uri("/review?productId=$PRODUCT_ID_OK")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.length()").isEqualTo(3)
            .jsonPath("\$[0].productId").isEqualTo(PRODUCT_ID_OK)
    }

    @Test
    fun `제품 아이디 잘못된 경우 검증`() {
        client.get()
            .uri("/review?productId=no-integer")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Type mismatch.")
    }

    @Test
    fun `제품 아이디로 리뷰 없는 경우 검증`() {
        client.get()
            .uri("/review?productId=$PRODUCT_ID_NOT_FOUND")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.length()").isEqualTo(0)
    }

    @Test
    fun `제품 아이디 음수 검증`() {
        client.get()
            .uri("/review?productId=$PRODUCT_ID_NEGATIVE")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/review")
            .jsonPath("\$.message").isEqualTo("Invalid productId: $PRODUCT_ID_NEGATIVE")
    }
}
