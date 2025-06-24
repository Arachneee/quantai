package com.quantai.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ReadPreference
import com.mongodb.WriteConcern
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import java.time.Duration
import java.util.concurrent.TimeUnit

@ConfigurationProperties(prefix = "spring.data.mongodb")
data class MongoProperties(
    val connection: ConnectionProperties = ConnectionProperties(),
    val readPreference: String = "primary",
    val writeConcern: WriteConcernProperties = WriteConcernProperties(),
    val retry: RetryProperties = RetryProperties(),
    val heartbeat: HeartbeatProperties = HeartbeatProperties(),
) {
    data class ConnectionProperties(
        val pool: PoolProperties = PoolProperties(),
        val socketTimeout: Duration = Duration.ofSeconds(5),
        val connectTimeout: Duration = Duration.ofSeconds(10),
        val serverSelectionTimeout: Duration = Duration.ofSeconds(15),
    )

    data class PoolProperties(
        val maxSize: Int = 100,
        val minSize: Int = 5,
        val maxWaitTime: Duration = Duration.ofSeconds(15),
        val maxConnectionLifeTime: Duration = Duration.ofSeconds(60),
        val maxConnectionIdleTime: Duration = Duration.ofSeconds(30),
    )

    data class WriteConcernProperties(
        val w: String = "majority",
        val journal: Boolean = true,
        val timeout: Duration = Duration.ofSeconds(5),
    )

    data class RetryProperties(
        val writes: Boolean = true,
        val reads: Boolean = true,
    )

    data class HeartbeatProperties(
        val frequency: Duration = Duration.ofSeconds(10),
        val minFrequency: Duration = Duration.ofMillis(500),
    )
}

@Configuration
@EnableConfigurationProperties(MongoProperties::class)
class MongoConfig(
    private val mongoProperties: MongoProperties,
) : AbstractReactiveMongoConfiguration() {
    @Value("\${spring.data.mongodb.uri}")
    private lateinit var connectionString: String

    @Value("\${spring.data.mongodb.database:stockdb}")
    private lateinit var database: String

    @Bean
    override fun reactiveMongoClient(): MongoClient {
        val settings =
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(connectionString))
                .applyToConnectionPoolSettings { builder ->
                    val pool = mongoProperties.connection.pool
                    builder.maxSize(pool.maxSize)
                        .minSize(pool.minSize)
                        .maxWaitTime(pool.maxWaitTime.toMillis(), TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(pool.maxConnectionLifeTime.toMillis(), TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(pool.maxConnectionIdleTime.toMillis(), TimeUnit.MILLISECONDS)
                }
                .applyToSocketSettings { builder ->
                    val conn = mongoProperties.connection
                    builder.connectTimeout(conn.connectTimeout.toMillis().toLong(), TimeUnit.MILLISECONDS)
                        .readTimeout(conn.socketTimeout.toMillis().toLong(), TimeUnit.MILLISECONDS)
                }
                .applyToServerSettings { builder ->
                    val heartbeat = mongoProperties.heartbeat
                    builder.heartbeatFrequency(heartbeat.frequency.toMillis(), TimeUnit.MILLISECONDS)
                        .minHeartbeatFrequency(heartbeat.minFrequency.toMillis(), TimeUnit.MILLISECONDS)
                }
                .readPreference(getReadPreference())
                .writeConcern(getWriteConcern())
                .retryWrites(mongoProperties.retry.writes)
                .retryReads(mongoProperties.retry.reads)
                .build()

        return MongoClients.create(settings)
    }

    private fun getReadPreference(): ReadPreference {
        return when (mongoProperties.readPreference.lowercase()) {
            "primary" -> ReadPreference.primary()
            "primarypreferred" -> ReadPreference.primaryPreferred()
            "secondary" -> ReadPreference.secondary()
            "secondarypreferred" -> ReadPreference.secondaryPreferred()
            "nearest" -> ReadPreference.nearest()
            else -> ReadPreference.primary()
        }
    }

    private fun getWriteConcern(): WriteConcern {
        val wc = mongoProperties.writeConcern
        return when (wc.w.lowercase()) {
            "majority" -> WriteConcern.MAJORITY
            "1" -> WriteConcern.W1
            "2" -> WriteConcern.W2
            "3" -> WriteConcern.W3
            else -> WriteConcern.ACKNOWLEDGED
        }.withJournal(wc.journal).withWTimeout(wc.timeout.toMillis(), TimeUnit.MILLISECONDS)
    }

    override fun getDatabaseName(): String {
        return database
    }

    @Bean
    fun reactiveMongoTemplate(): ReactiveMongoTemplate {
        return ReactiveMongoTemplate(reactiveMongoClient(), databaseName)
    }
}
