package dev.minz.microservices.core.product

import dev.minz.microservices.core.product.persistence.ProductEntity
import dev.minz.microservices.core.product.persistence.ProductRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.stream.Collectors
import java.util.stream.IntStream

@ExtendWith(SpringExtension::class)
@DataMongoTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersistenceTest {
    @Autowired
    private lateinit var repository: ProductRepository

    private lateinit var savedEntity: ProductEntity

    @BeforeEach
    fun setupDb() {
        repository.deleteAll()

        val entity = ProductEntity(1, "n", 1)
        savedEntity = repository.save(entity)
    }

    @Test
    fun create() {
        val newEntity = ProductEntity(2, "n", 2)
        repository.save(newEntity)
        val foundEntity = repository.findByIdOrNull(newEntity.id)
        assertEqualsProduct(newEntity, foundEntity!!)
        assertEquals(2, repository.count())
    }

    @Test
    fun update() {
        savedEntity.name = "n2"
        repository.save(savedEntity)
        val foundEntity = repository.findByIdOrNull(savedEntity.id)
        assertEquals(1, foundEntity?.version)
        assertEquals("n2", foundEntity?.name)
    }

    @Test
    fun delete() {
        repository.delete(savedEntity)
        assertFalse(repository.existsById(savedEntity.id!!))
    }

    @Test
    fun getByProductId() {
        val entity = repository.findByProductId(savedEntity.productId)
        assertEqualsProduct(savedEntity, entity!!)
    }

    @Test
    fun duplicateError() {
        assertThrows<DuplicateKeyException> {
            val entity = ProductEntity(savedEntity.productId, "n", 1)
            repository.save(entity)
        }
    }

    @Test
    fun optimisticLockError() {

        // Store the saved entity in two separate entity objects
        val entity1 = repository.findByIdOrNull(savedEntity.id)
        val entity2 = repository.findByIdOrNull(savedEntity.id)

        // Update the entity using the first entity object
        entity1?.name = "n1"
        repository.save(entity1!!)

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
        assertThrows<OptimisticLockingFailureException> {
            entity2?.name = "n2"
            repository.save(entity2!!)
        }

        // Get the updated entity from the database and verify its new sate
        val updatedEntity = repository.findByIdOrNull(savedEntity.id)
        assertEquals(1, updatedEntity?.version)
        assertEquals("n1", updatedEntity?.name)
    }

    @Test
    fun paging() {
        repository.deleteAll()
        val newProducts = IntStream.rangeClosed(1001, 1010)
            .mapToObj { i: Int -> ProductEntity(i, "name $i", i) }
            .collect(Collectors.toList())
        repository.saveAll(newProducts)
        var nextPage: Pageable? = PageRequest.of(0, 4, Sort.Direction.ASC, "productId")
        nextPage = testNextPage(nextPage!!, "[1001, 1002, 1003, 1004]", true)
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true)
        nextPage = testNextPage(nextPage, "[1009, 1010]", false)
    }

    private fun testNextPage(nextPage: Pageable, expectedProductIds: String, expectsNextPage: Boolean): Pageable {
        val productPage = repository.findAll(nextPage)
        assertEquals(
            expectedProductIds,
            productPage.content.stream().map<Any> { p: ProductEntity -> p.productId }
                .collect(Collectors.toList()).toString()
        )
        assertEquals(expectsNextPage, productPage.hasNext())
        return productPage.nextPageable()
    }

    private fun assertEqualsProduct(expectedEntity: ProductEntity, actualEntity: ProductEntity) {
        assertEquals(expectedEntity.id, actualEntity.id)
        assertEquals(expectedEntity.version, actualEntity.version)
        assertEquals(expectedEntity.productId, actualEntity.productId)
        assertEquals(expectedEntity.name, actualEntity.name)
        assertEquals(expectedEntity.weight, actualEntity.weight)
    }
}
