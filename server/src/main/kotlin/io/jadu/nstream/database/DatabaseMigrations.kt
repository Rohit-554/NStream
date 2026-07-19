package io.jadu.nstream.database

import io.jadu.nstream.config.ServerConfig
import org.flywaydb.core.Flyway

object DatabaseMigrations {
    fun migrate(config: ServerConfig){
        Flyway.configure()
            .dataSource(
                config.databaseUrl, config.databaseUser, config.databasePassword
            )
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}