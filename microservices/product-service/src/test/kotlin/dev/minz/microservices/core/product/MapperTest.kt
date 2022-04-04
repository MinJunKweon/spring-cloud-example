package dev.minz.microservices.core.product

import dev.minz.api.core.product.Product
import dev.minz.microservices.core.product.services.ProductMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mapstruct.factory.Mappers

class MapperTest {
    private val mapper = Mappers.getMapper(ProductMapper::class.java)

    @Test
    fun `매퍼 테스트`() {
        val api = Product(1, "n", 1, "sa")

        val entity = mapper.apiToEntity(api)

        assertEquals(api.productId, entity.productId)
        assertEquals(api.name, entity.name)
        assertEquals(api.weight, entity.weight)

        val api2 = mapper.entityToApi(entity)

        assertEquals(api.productId, api2.productId)
        assertEquals(api.name, api2.name)
        assertEquals(api.weight, api2.weight)
        assertNull(api2.serviceAddress)
    }
}
