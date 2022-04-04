package dev.minz.microservices.core.review

import dev.minz.microservices.core.review.persistence.ReviewEntity
import dev.minz.microservices.core.review.persistence.ReviewRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@ExtendWith(SpringExtension::class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PersistenceTest {
    @Autowired
    private lateinit var repository: ReviewRepository

    private lateinit var savedEntity: ReviewEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()

        val entity = ReviewEntity(1, 2, "a", "s", "c")
        savedEntity = repository.save(entity)

        assertEqualsReview(entity, savedEntity)
    }

    @Test
    fun create() {
        val newEntity = ReviewEntity(1, 3, "a", "s", "c")
        repository.save(newEntity)
        val foundEntity = repository.findByIdOrNull(newEntity.id!!)
        assertEqualsReview(newEntity, foundEntity!!)
        Assertions.assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity.author = "a2"
        repository.save(savedEntity)
        val foundEntity = repository.findByIdOrNull(savedEntity.id!!)!!
        Assertions.assertEquals(1, foundEntity.version)
        Assertions.assertEquals("a2", foundEntity.author)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity)
        Assertions.assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val entityList = repository.findByProductId(savedEntity.productId!!)
        Assertions.assertEquals(1, entityList.size)
        assertEqualsReview(savedEntity, entityList[0])
    }

    @Test
    fun duplicateError() {
        val entity = ReviewEntity(1, 2, "a", "s", "c")
        assertThrows<DataIntegrityViolationException> {
            repository.save(entity)
        }
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findByIdOrNull(savedEntity.id!!)!!
        val entity2 = repository.findByIdOrNull(savedEntity.id!!)!!

        // Update the entity using the first entity object
        entity1.author = "a1"
        repository.save(entity1)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        assertThrows<OptimisticLockingFailureException> {
            entity2.author = "a2"
            repository.save(entity2)
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findByIdOrNull(savedEntity.id!!)
        Assertions.assertEquals(1, updatedEntity?.version)
        Assertions.assertEquals("a1", updatedEntity?.author)
    }

    private fun assertEqualsReview(expectedEntity: ReviewEntity, actualEntity: ReviewEntity) {
        Assertions.assertEquals(expectedEntity.id, actualEntity.id)
        Assertions.assertEquals(expectedEntity.version, actualEntity.version)
        Assertions.assertEquals(expectedEntity.productId, actualEntity.productId)
        Assertions.assertEquals(expectedEntity.reviewId, actualEntity.reviewId)
        Assertions.assertEquals(expectedEntity.author, actualEntity.author)
        Assertions.assertEquals(expectedEntity.subject, actualEntity.subject)
        Assertions.assertEquals(expectedEntity.content, actualEntity.content)
    }
}
