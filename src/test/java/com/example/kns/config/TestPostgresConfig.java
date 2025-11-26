package com.example.kns.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class TestPostgresConfig {
    // embeddedPostgres bean -> produces dataSource bean -> injects into MyBatis and Flyway

    // embedded postgres bean
    @Bean
    public DataSource dataSource(EmbeddedPostgres pg) {
        return pg.getPostgresDatabase();
    }

    // datasource bean
    @Bean(destroyMethod = "close")
    public EmbeddedPostgres embeddedPostgres() throws Exception {
        return EmbeddedPostgres.builder().start();
    }
}