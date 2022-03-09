package dev.minz.microservices.core.product

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 13
        private const val PRODUCT_ID_NEGATIVE = -1
    }

    @Autowired
    private lateinit var client: WebTestClient

    @Test
    fun `제품 아이디로 검색 검증`() {
        client.get()
            .uri("/product/$PRODUCT_ID_OK")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.productId").isEqualTo(PRODUCT_ID_OK)
    }

    @Test
    fun `제품 아이디 없는 경우 검증`() {
        client.get()
            .uri("/product/$PRODUCT_ID_NOT_FOUND")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/product/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("\$.message").isEqualTo("No product found for productId: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun `제품 아이디 잘못된 경우 검증`() {
        client.get()
            .uri("/product/no-integer")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/product/no-integer")
    }

    @Test
    fun `제품 아이디 음수 입수 검증`() {
        client.get()
            .uri("/product/$PRODUCT_ID_NEGATIVE")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.path").isEqualTo("/product/$PRODUCT_ID_NEGATIVE")
            .jsonPath("\$.message").isEqualTo("Invalid productId: $PRODUCT_ID_NEGATIVE")
    }
}
