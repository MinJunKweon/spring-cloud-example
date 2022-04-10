package dev.minz.microservices.core.product

import dev.minz.microservices.core.product.persistence.ProductEntity
import dev.minz.microservices.core.product.persistence.ProductRepository
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
    private lateinit var repository: ProductRepository

    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setupDb() = runBlocking {
        repository.deleteAll()

        val entity = ProductEntity(1, "n", 1)
        savedEntity = repository.save(entity)
    }

    @Test
    fun create() = runBlocking {
        val newEntity = ProductEntity(2, "n", 2)
        val result = repository.save(newEntity)
        assertEquals(newEntity.productId, result.productId)

        assertEquals(newEntity, repository.findById(newEntity.id!!))

        assertEquals(2L, repository.count())
    }

    @Test
    fun update() = runBlocking {
        savedEntity.name = "n2"
        repository.save(savedEntity).also {
            assertEquals(savedEntity.name, it.name)
        }

        repository.findById(savedEntity.id!!)!!.let {
            assertEquals(1, it.version)
            assertEquals("n2", it.name)
        }
    }

    @Test
    fun delete() = runBlocking {
        repository.delete(savedEntity)
        assertFalse { repository.existsById(savedEntity.id!!) }
    }

    @Test
    fun getByProductId() = runBlocking {
        val actualResult = repository.findByProductId(savedEntity.productId)

        assertEquals(savedEntity, actualResult)
    }

    @Test
    fun duplicateError() = runBlocking {
        val entity = ProductEntity(savedEntity.productId, "n", 1)
        val actualException = assertThrows<DuplicateKeyException> {
            repository.save(entity)
        }
        assertEquals(DuplicateKeyException::class, actualException::class)
    }

    @Test
    fun optimisticLockError() = runBlocking {
        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!)
        val entity2 = repository.findById(savedEntity.id!!)

        // Update the entity using the first entity object
        entity1?.name = "n1"
        repository.save(entity1!!)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        entity2?.name = "n2"
        assertThrows<OptimisticLockingFailureException> {
            repository.save(entity2!!)
        }

        // Get the updated entity from the database and verify its new sate
        val actualResult = repository.findById(savedEntity.id!!)

        assertEquals(1, actualResult?.version)
        assertEquals("n1", actualResult?.name)
    }
}
