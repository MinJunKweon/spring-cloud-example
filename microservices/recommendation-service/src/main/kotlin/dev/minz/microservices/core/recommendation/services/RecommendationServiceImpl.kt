package dev.minz.microservices.core.recommendation.services

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import org.slf4j.LoggerFactory
import org.springframework.dao.DuplicateKeyException
import org.springframework.web.bind.annotation.RestController

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
        val newEntity = try {
            repository.save(entity)
        } catch (dke: DuplicateKeyException) {
            throw InvalidInputException(
                "Duplicate key, product Id: ${body.productId}, recommendation Id : ${body.recommendationId}"
            )
        }

        LOG.debug("createRecommendation: created a recommendation entity: ${body.productId}/${body.recommendationId}")
        return mapper.entityToApi(newEntity)
    }

    override fun getRecommendations(productId: Int): List<Recommendation>? {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        val entityList = repository.findByProductId(productId)
        val list = mapper.entityListToApiList(entityList)
        list.forEach { it.serviceAddress = serviceUtil.serviceAddress }

        LOG.debug("getRecommendations: response size: ${list.size}")

        return list
    }

    override fun deleteRecommendations(productId: Int) {
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: $productId")
        repository.deleteAll(repository.findByProductId(productId))
    }
}
