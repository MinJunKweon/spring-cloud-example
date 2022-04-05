package dev.minz.microservices.core.recommendation.services

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
class RecommendationServiceImpl(
    private val repository: RecommendationRepository,
    private val mapper: RecommendationMapper,
    private val serviceUtil: ServiceUtil,
) : RecommendationService {
    companion object {
        private val LOG = LoggerFactory.getLogger(RecommendationServiceImpl::class.java)
    }

    override fun createRecommendation(body: Recommendation): Recommendation {
        val entity = mapper.apiToEntity(body)
        val savedRecommendation = repository.save(entity)
            .log()
            .onErrorMap(DuplicateKeyException::class.java) {
                InvalidInputException(
                    "Duplicate key, product Id: ${body.productId}, recommendation Id : ${body.recommendationId}"
                )
            }
            .map { mapper.entityToApi(it) }
            .block()
        return checkNotNull(savedRecommendation) { "Recommendation must be not null. Product Id: ${body.productId}" }
    }

    override fun getRecommendations(productId: Int): Flux<Recommendation> {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        return repository.findByProductId(productId)
            .log()
            .map { mapper.entityToApi(it) }
            .map {
                it.serviceAddress = serviceUtil.serviceAddress
                it
            }
    }

    override fun deleteRecommendations(productId: Int) {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: $productId")
        repository.deleteAll(repository.findByProductId(productId)).block()
    }
}
