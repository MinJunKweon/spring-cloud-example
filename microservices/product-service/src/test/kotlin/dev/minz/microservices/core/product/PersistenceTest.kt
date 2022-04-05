package dev.minz.microservices.core.product

import dev.minz.microservices.core.product.persistence.ProductEntity
import dev.minz.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.test.StepVerifier

@ExtendWith(SpringExtension::class)
@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersistenceTest {
    @Autowired
    private lateinit var repository: ProductRepository

    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setupDb() {
        StepVerifier.create(repository.deleteAll()).verifyComplete()

        val entity = ProductEntity(1, "n", 1)
        StepVerifier.create(repository.save(entity))
            .expectNextMatches {
                savedEntity = it
                savedEntity == entity
            }
            .verifyComplete()
    }

    @Test
    fun create() {
        val newEntity = ProductEntity(2, "n", 2)
        StepVerifier.create(repository.save(newEntity))
            .expectNextMatches { newEntity.productId == it.productId }
            .verifyComplete()

        StepVerifier.create(repository.findById(newEntity.id!!))
            .expectNextMatches { it == newEntity }
            .verifyComplete()

        StepVerifier.create(repository.count())
            .expectNext(2L)
            .verifyComplete()
    }

    @Test
    fun update() {
        savedEntity.name = "n2"
        StepVerifier.create(repository.save(savedEntity))
            .expectNextMatches { savedEntity.name == it.name }
            .verifyComplete()

        StepVerifier.create(repository.findById(savedEntity.id!!))
            .expectNextMatches { it.version == 1 && it.name == "n2" }
            .verifyComplete()
    }

    @Test
    fun delete() {
        StepVerifier.create(repository.delete(savedEntity)).verifyComplete()
        StepVerifier.create(repository.existsById(savedEntity.id!!)).expectNext(false).verifyComplete()
    }

    @Test
    fun getByProductId() {
        StepVerifier.create(repository.findByProductId(savedEntity.productId))
            .expectNextMatches { it == savedEntity }
            .verifyComplete()
    }

    @Test
    fun duplicateError() {
        val entity = ProductEntity(savedEntity.productId, "n", 1)
        StepVerifier.create(repository.save(entity))
            .expectError(DuplicateKeyException::class.java)
            .verify()
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findById(savedEntity.id!!).block()
        val entity2 = repository.findById(savedEntity.id!!).block()

        // Update the entity using the first entity object
        entity1?.name = "n1"
        repository.save(entity1!!).block()

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        entity2?.name = "n2"
        StepVerifier.create(repository.save(entity2!!))
            .expectError(OptimisticLockingFailureException::class.java)
            .verify()

        // Get the updated entity from the database and verify its new sate
        StepVerifier.create(repository.findById(savedEntity.id!!))
            .expectNextMatches { it.version == 1 && it.name == "n1" }
            .verifyComplete()
    }
}
