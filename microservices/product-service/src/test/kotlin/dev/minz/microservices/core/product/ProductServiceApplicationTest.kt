package dev.minz.microservices.core.product

import dev.minz.api.core.product.Product
import dev.minz.api.core.product.ProductService
import dev.minz.microservices.core.product.persistence.ProductRepository
import dev.minz.util.exceptions.InvalidInputException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.BodyContentSpec

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.data.mongodb.port: 0"]
)
class ProductServiceApplicationTest {

    @Autowired
    private lateinit var client: WebTestClient

    @Autowired
    private lateinit var repository: ProductRepository

    @Autowired
    private lateinit var productService: ProductService

    @BeforeEach
    fun setupDb() {
        repository.deleteAll().block()
    }

    @Test
    fun `제품 아이디로 검색 검증`() {
        productService.createProduct(createProduct(1))
        Assertions.assertTrue(repository.findByProductId(1).block() != null)

        getAndVerifyProduct(1, HttpStatus.OK)
            .jsonPath("\$.productId").isEqualTo(1)
    }

    @Test
    fun `중복 제품 검증`() {
        val product = createProduct(1)
        productService.createProduct(product)
        Assertions.assertTrue(repository.findByProductId(1).block() != null)

        assertThrows<InvalidInputException> {
            productService.createProduct(product)
        }
    }

    @Test
    fun `제품 삭제 검증`() {
        productService.createProduct(createProduct(1))
        Assertions.assertTrue(repository.findByProductId(1).block() != null)

        productService.deleteProduct(1)
        Assertions.assertFalse(repository.findByProductId(1).block() != null)

        productService.deleteProduct(1)
    }

    @Test
    fun `잘못된 제품 아이디 검증`() {
        getAndVerifyProduct("/no-integer", HttpStatus.BAD_REQUEST)
            .jsonPath("\$.path").isEqualTo("/product/no-integer")
    }

    @Test
    fun `제품 아이디 없는 경우 검증`() {
        getAndVerifyProduct(13, HttpStatus.NOT_FOUND)
            .jsonPath("\$.path").isEqualTo("/product/13")
            .jsonPath("\$.message").isEqualTo("No product found for productId: 13")
    }

    @Test
    fun `제품 아이디 음수 입수 검증`() {
        getAndVerifyProduct(-1, HttpStatus.UNPROCESSABLE_ENTITY)
            .jsonPath("\$.path").isEqualTo("/product/-1")
            .jsonPath("\$.message").isEqualTo("Invalid productId: -1")
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

    private fun createProduct(productId: Int) =
        Product(productId, "Name $productId", productId, "SA")
}
