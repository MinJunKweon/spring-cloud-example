package dev.minz.microservices.core.review.configuration

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.util.concurrent.Executors

@Configuration
class ThreadPoolExecutorConfiguration {
    companion object {
        private val LOG = LoggerFactory.getLogger(ThreadPoolExecutorConfiguration::class.java)
    }

    @Bean
    fun threadPoolScheduler(
        @Value("\${spring.datasource.maximum-pool-size:10}") connectionPoolSize: Int,
    ): Scheduler {
        LOG.info("Creates a threadPoolScheduler with connectionPoolSize = $connectionPoolSize")
        return Schedulers.fromExecutor(Executors.newFixedThreadPool(connectionPoolSize))
    }
}
