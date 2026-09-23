package com.tana.tana_auth.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ApplicationRunner deploymentMigrations(DataSource dataSource) {
        // Preserve Hibernate table creation and the deferred SQL seed scripts. Runners
        // execute afterward, before readiness and before the existing Excel importer.
        return args -> Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            // Isolate deployment migrations from the historical, manually applied scripts.
            .table("tana_deployment_schema_history")
            .baselineOnMigrate(true)
            .load()
            .migrate();
    }
}
