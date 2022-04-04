package dev.minz.microservices.core.review.persistence

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table
import javax.persistence.Version

@Entity
@Table(
    name = "reviews",
    indexes = [Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")]
)
class ReviewEntity(
    var productId: Int,
    var reviewId: Int,
    var author: String,
    var subject: String,
    var content: String,
) {
    @Id
    @GeneratedValue
    var id: Int? = null

    @Version
    var version: Int? = null
}
