package dev.minz.springcloud.eurekaserver

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class EurekaServerApplicationTest {

    @Test
    fun contextLoads() {
        // Nothing to do...
    }

    @Autowired
    private lateinit var testWebClient: WebTestClient

    @Test
    fun `카탈로그 검색 검증`() {
        testWebClient.get()
            .uri("/eureka/apps")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody()
            .jsonPath("\$.applications.versions__delta").isEqualTo("1")
            .jsonPath("\$.applications.apps__hashcode").isEqualTo("")
            .jsonPath("\$.applications.application").isEmpty
    }

    @Test
    fun `상태 헬스체크 검증`() {
        testWebClient.get()
            .uri("/actuator/health")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody()
            .jsonPath("\$.status").isEqualTo("UP")
    }
}
