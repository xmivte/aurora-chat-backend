package com.example.kns.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@TestConfiguration
public class TestEmbeddedPostgresConfig {
	// embedded bean produces dataSource bean & injects into myBatis & flyway

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