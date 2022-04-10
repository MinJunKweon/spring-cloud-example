package dev.minz.microservices.core.recommendation.services

import dev.minz.api.core.recommendation.Recommendation
import dev.minz.api.core.recommendation.RecommendationService
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import dev.minz.util.exceptions.InvalidInputException
import dev.minz.util.http.ServiceUtil
import kotlinx.coroutines.runBlocking
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
        val savedRecommendation = runBlocking {
            val result = try {
                repository.save(entity)
            } catch (e: DuplicateKeyException) {
                throw InvalidInputException(
                    "Duplicate key, product Id: ${body.productId}, recommendation Id : ${body.recommendationId}"
                )
            }
            mapper.entityToApi(result)
        }
        return savedRecommendation
    }

    override suspend fun getRecommendations(productId: Int): List<Recommendation> {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }

        return repository.findByProductId(productId).map {
            mapper.entityToApi(it).apply {
                serviceAddress = serviceUtil.serviceAddress
            }
        }
    }

    override fun deleteRecommendations(productId: Int) {
        require(productId > 0) { throw InvalidInputException("Invalid productId: $productId") }
        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: $productId")
        runBlocking {
            repository.deleteAll(repository.findByProductId(productId))
        }
    }
}
