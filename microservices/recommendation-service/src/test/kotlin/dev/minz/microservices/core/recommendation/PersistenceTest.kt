package dev.minz.microservices.core.recommendation

import dev.minz.microservices.core.recommendation.persistence.RecommendationEntity
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersistenceTest {
    @Autowired
    private lateinit var repository: RecommendationRepository

    private lateinit var savedEntity: RecommendationEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()

        val entity = RecommendationEntity(1, 2, "a", 3, "c")
        savedEntity = repository.save(entity)

        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create() {
        val entity = RecommendationEntity(1, 3, "a", 3, "c")
        val newEntity = repository.save(entity)
        val foundEntity = repository.findByIdOrNull(newEntity.id!!)
        assertEqualsRecommendation(newEntity, foundEntity!!)
        Assertions.assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity.author = "a2"
        val updatedEntity = repository.save(savedEntity)

        val foundEntity = repository.findByIdOrNull(updatedEntity.id!!)
        Assertions.assertEquals(1, foundEntity?.version)
        Assertions.assertEquals("a2", foundEntity?.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity)
        Assertions.assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val entityList = repository.findByProductId(savedEntity.productId)

        Assertions.assertEquals(1, entityList.size)
        assertEqualsRecommendation(savedEntity, entityList.first())
    }

    @Test
    fun duplicateError() {
        assertThrows<DuplicateKeyException> {
            val entity = RecommendationEntity(1, 2, "a", 3, "c")
            repository.save(entity)
        }
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findByIdOrNull(savedEntity.id!!)
        val entity2 = repository.findByIdOrNull(savedEntity.id!!)

        // Update the entity using the first entity object
        entity1?.author = "a1"
        repository.save(entity1!!)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        assertThrows<OptimisticLockingFailureException> {
            entity2?.author = "a2"
            repository.save(entity2!!)
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findByIdOrNull(savedEntity.id!!)
        Assertions.assertEquals(1, updatedEntity?.version)
        Assertions.assertEquals("a1", updatedEntity?.author)
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity, actualEntity: RecommendationEntity) {
        Assertions.assertEquals(expectedEntity.id, actualEntity.id)
        Assertions.assertEquals(expectedEntity.version, actualEntity.version)
        Assertions.assertEquals(expectedEntity.productId, actualEntity.productId)
        Assertions.assertEquals(expectedEntity.recommendationId, actualEntity.recommendationId)
        Assertions.assertEquals(expectedEntity.author, actualEntity.author)
        Assertions.assertEquals(expectedEntity.rating, actualEntity.rating)
        Assertions.assertEquals(expectedEntity.content, actualEntity.content)
    }
}
