package dev.minz.api.event

import java.time.LocalDateTime

data class Event<K, T>(
    val eventType: Type,
    val key: K,
    val data: T,
    val eventCreatedAt: LocalDateTime = LocalDateTime.now(),
) {
    enum class Type { CREATE, DELETE }
}
