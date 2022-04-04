package dev.minz.microservices.core.review.services

import dev.minz.api.core.review.Review
import dev.minz.microservices.core.review.persistence.ReviewEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Mappings

@Mapper(componentModel = "spring")
interface ReviewMapper {

    @Mappings(
        value = [
            Mapping(target = "serviceAddress", ignore = true),
        ]
    )
    fun entityToApi(entity: ReviewEntity): Review

    @Mappings(
        value = [
            Mapping(target = "id", ignore = true),
            Mapping(target = "version", ignore = true)
        ]
    )
    fun apiToEntity(api: Review): ReviewEntity

    fun entityListToApiList(entity: List<ReviewEntity>): List<Review>
    fun apiListToEntityList(api: List<Review>): List<ReviewEntity>
}
