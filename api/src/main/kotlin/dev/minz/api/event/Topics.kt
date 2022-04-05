package dev.minz.api.event

enum class Topics(
    val topicName: String,
) {
    PRODUCT("product"),
    RECOMMENDATION("recommendation"),
    REVIEW("review"),
}
