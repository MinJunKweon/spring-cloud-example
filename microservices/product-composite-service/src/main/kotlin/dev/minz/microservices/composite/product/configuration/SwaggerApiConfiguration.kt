package dev.minz.microservices.composite.product.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.web.bind.annotation.RequestMethod.GET
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors.basePackage
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerApiConfiguration(
    /* ktlint-disable no-multi-spaces */
    // @formatter:off
    @Value("\${api.common.version}")           private val apiVersion: String,
    @Value("\${api.common.title}")             private val apiTitle: String,
    @Value("\${api.common.description}")       private val apiDescription: String,
    @Value("\${api.common.termsOfServiceUrl}") private val apiTermsOfServiceUrl: String,
    @Value("\${api.common.license}")           private val apiLicense: String,
    @Value("\${api.common.licenseUrl}")        private val apiLicenseUrl: String,
    @Value("\${api.common.contact.name}")      private val apiContactName: String,
    @Value("\${api.common.contact.url}")       private val apiContactUrl: String,
    @Value("\${api.common.contact.email}")     private val apiContactEmail: String,
    // @formatter:on
    /* ktlint-enable no-multi-spaces */
) {

    @Bean
    fun apiDocumentation(): Docket =
        Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(basePackage("dev.minz.microservices.composite.product"))
            .paths(PathSelectors.any())
            .build()
            .globalResponseMessage(GET, emptyList())
            .apiInfo(
                ApiInfo(
                    /* ktlint-disable no-multi-spaces */
                    // @formatter:off
                    /* title = */ apiTitle,
                    /* description = */ apiDescription,
                    /* version = */ apiVersion,
                    /* termsOfServiceUrl = */ apiTermsOfServiceUrl,
                    /* contact = */ Contact(apiContactName, apiContactUrl, apiContactEmail),
                    /* license = */ apiLicense,
                    /* licenseUrl = */ apiLicenseUrl,
                    /* vendorExtensions = */ emptyList(),
                    // @formatter:on
                    /* ktlint-enable no-multi-spaces */
                )
            )

    @Bean
    fun springFoxSupplier(): () -> Message<String>? = { null }
}
