package dev.minz.util.http

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.InetAddress

@Component
class ServiceUtil(
    @Value("\${server.port}") private val port: String,
) {
    val serviceAddress: String
        get() = "${findMyHostname()}/${findMyIpAddress()}:$port"

    private fun findMyHostname(): String =
        runCatching { InetAddress.getLocalHost().hostName }
            .getOrElse { "unknown host name" }

    private fun findMyIpAddress(): String =
        runCatching { InetAddress.getLocalHost().hostAddress }
            .getOrElse { "unknown IP address" }
}
