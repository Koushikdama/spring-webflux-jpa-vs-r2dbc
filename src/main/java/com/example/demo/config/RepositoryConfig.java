package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/*
 * JPA and R2DBC are both on the classpath. Because User has @Entity, JPA would try to own
 * EVERY repository of User — including the reactive one — and fail with
 * "Reactive Repositories are not supported by JPA". So each module gets its own package.
 */
@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories(basePackages = "com.example.demo.repository.jpa")
@EnableR2dbcRepositories(basePackages = "com.example.demo.repository.r2dbc")
public class RepositoryConfig {
}
