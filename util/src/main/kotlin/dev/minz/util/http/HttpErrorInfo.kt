package dev.minz.util.http

import org.springframework.http.HttpStatus
import java.time.ZonedDateTime

data class HttpErrorInfo(
    private val httpStatus: HttpStatus,
    val path: String,
    val message: String?,
) {
    val timestamp: ZonedDateTime = ZonedDateTime.now()
    val status: Int = httpStatus.value()
    val error: String = httpStatus.reasonPhrase
}
