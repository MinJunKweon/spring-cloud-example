package dev.minz.microservices.core.recommendation

import dev.minz.microservices.core.recommendation.persistence.RecommendationEntity
import dev.minz.microservices.core.recommendation.persistence.RecommendationRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExtendWith(SpringExtension::class)
@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersistenceTest {
    @Autowired
    private lateinit var repository: RecommendationRepository

    private lateinit var savedEntity: RecommendationEntity

    @BeforeEach
    fun setupDb() = runBlocking {
        repository.deleteAll()

        val entity = RecommendationEntity(1, 2, "a", 3, "c")
        savedEntity = requireNotNull(repository.save(entity))

        assertEqualsRecommendation(entity, savedEntity)
    }

    @Test
    fun create() = runBlocking {
        val entity = RecommendationEntity(1, 3, "a", 3, "c")
        val newEntity = repository.save(entity)
        val foundEntity = repository.findById(newEntity.id!!)!!
        assertEqualsRecommendation(newEntity, foundEntity)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() = runBlocking {
        savedEntity.author = "a2"
        val updatedEntity = repository.save(savedEntity)

        val foundEntity = repository.findById(updatedEntity.id!!)
        assertEquals(1, foundEntity?.version)
        assertEquals("a2", foundEntity?.author)
    }

    @Test
    fun delete() = runBlocking {
        repository.delete(savedEntity)
        assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() = runBlocking {
        val entityList = repository.findByProductId(savedEntity.productId)

        assertEquals(1, entityList.size)
        assertEqualsRecommendation(savedEntity, entityList.first())
    }

    @Test
    fun duplicateError() = runBlocking {
        assertThrows<DuplicateKeyException> {
            val entity = RecommendationEntity(1, 2, "a", 3, "c")
            repository.save(entity)
        }.let {
            assertEquals(DuplicateKeyException::class, it::class)
        }
    }

    @Test
    fun optimisticLockError() = runBlocking {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!)
        val entity2 = repository.findById(savedEntity.id!!)

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
        val updatedEntity = repository.findById(savedEntity.id!!)
        assertEquals(1, updatedEntity?.version)
        assertEquals("a1", updatedEntity?.author)
    }

    private fun assertEqualsRecommendation(expectedEntity: RecommendationEntity, actualEntity: RecommendationEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.recommendationId, actualEntity.recommendationId)
        assertEquals(expectedEntity.author, actualEntity.author)
        assertEquals(expectedEntity.rating, actualEntity.rating)
        assertEquals(expectedEntity.content, actualEntity.content)
    }
}
