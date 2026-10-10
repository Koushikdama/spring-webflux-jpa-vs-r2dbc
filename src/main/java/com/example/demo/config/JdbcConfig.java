package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Why this class exists:
 * Spring Boot's DataSourceAutoConfiguration has
 *   @ConditionalOnMissingBean(type = "io.r2dbc.spi.ConnectionFactory")
 * so as soon as R2DBC is on the classpath, NO DataSource is auto-created.
 * Boot assumes "reactive app = no JDBC". We want both (to compare them),
 * so we declare the DataSource ourselves. Once it exists, Boot's
 * JdbcTemplate / NamedParameterJdbcTemplate / JdbcClient auto-config kicks in again.
 */
@Configuration(proxyBeanMethods = false)
public class JdbcConfig {

    @Bean
    @ConfigurationProperties("spring.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    HikariDataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
