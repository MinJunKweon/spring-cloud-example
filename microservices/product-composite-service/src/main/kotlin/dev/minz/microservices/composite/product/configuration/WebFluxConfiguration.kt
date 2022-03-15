package dev.minz.microservices.composite.product.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class WebFluxConfiguration {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
