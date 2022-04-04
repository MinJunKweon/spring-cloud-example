package dev.minz.microservices.core.product

import dev.minz.api.core.product.Product
import dev.minz.microservices.core.product.persistence.ProductRepository
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
class ProductServiceApplicationTest {

    companion object {
        private const val PRODUCT_ID_OK = 1
        private const val PRODUCT_ID_NOT_FOUND = 13
        private const val PRODUCT_ID_NEGATIVE = -1
    }

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ProductRepository

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()
    }

    @Test
    fun `제품 아이디로 검색 검증`() {
        postAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)

        Assertions.assertTrue(repository.findByProductId(PRODUCT_ID_OK) != null)
        getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
            .jsonPath("\$.productId").isEqualTo(PRODUCT_ID_OK)
    }

    @Test
    fun `중복 제품 검증`() {
        postAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
        Assertions.assertTrue(repository.findByProductId(PRODUCT_ID_OK) != null)

        postAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/product")
            .jsonPath("\$.message").isEqualTo("Duplicate key, product id: $PRODUCT_ID_OK")
    }

    @Test
    fun `제품 삭제 검증`() {
        postAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
        Assertions.assertTrue(repository.findByProductId(PRODUCT_ID_OK) != null)

        deleteAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
        Assertions.assertFalse(repository.findByProductId(PRODUCT_ID_OK) != null)

        deleteAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
    }

    @Test
    fun `잘못된 제품 아이디 검증`() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("\$.path").isEqualTo("/product/no-integer")
    }

    @Test
    fun `제품 아이디 없는 경우 검증`() {
        getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
            .jsonPath("\$.path").isEqualTo("/product/$PRODUCT_ID_NOT_FOUND")
            .jsonPath("\$.message").isEqualTo("No product found for productId: $PRODUCT_ID_NOT_FOUND")
    }

    @Test
    fun `제품 아이디 음수 입수 검증`() {
        getAndVerifyProduct(PRODUCT_ID_NEGATIVE, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/product/$PRODUCT_ID_NEGATIVE")
            .jsonPath("\$.message").isEqualTo("Invalid productId: $PRODUCT_ID_NEGATIVE")
    }

    private fun getAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return getAndVerifyProduct("/$productId", expectedStatus)
    }

    private fun getAndVerifyProduct(productIdPath: String, expectedStatus: HttpStatus): BodyContentSpec {
        return client.get()
            .uri("/product$productIdPath")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun postAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        val product = Product(productId, "Name $productId", productId, "SA")
        return client.post()
            .uri("/product")
            .body(Mono.just(product), Product::class.java)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
    }

    private fun deleteAndVerifyProduct(productId: Int, expectedStatus: HttpStatus): BodyContentSpec {
        return client.delete()
            .uri("/product/$productId")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedStatus)
            .expectBody()
    }
}
