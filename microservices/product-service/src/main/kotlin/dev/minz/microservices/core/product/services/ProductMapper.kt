package dev.minz.microservices.core.product.services

import dev.minz.api.core.product.Product
import dev.minz.microservices.core.product.persistence.ProductEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface ProductMapper {
    @Mappings(
        value = [
            Mapping(target = "serviceAddress", ignore = true),
        ]
    )
    fun entityToApi(entity: ProductEntity): Product

    @Mappings(
        value = [
            Mapping(target = "id", ignore = true),
            Mapping(target = "version", ignore = true),
        ]
    )
    fun apiToEntity(api: Product): ProductEntity
}
