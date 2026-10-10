package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.test.StepVerifier;

/**
 * Same MySQL database (reactive_practice), two drivers:
 *   JdbcClient     -> blocking, returns the value directly
 *   DatabaseClient -> reactive, returns a Mono/Flux that does nothing until subscribed
 */
@SpringBootTest
class JdbcVsR2dbcTest {

    @Autowired JdbcClient jdbc;
    @Autowired DatabaseClient r2dbc;

    @Test
    void writeWithJdbc_readWithR2dbc() {
        jdbc.sql("CREATE TABLE IF NOT EXISTS person(id INT PRIMARY KEY, name VARCHAR(50))").update();
        jdbc.sql("INSERT INTO person(id, name) VALUES (1, 'Koushik') ON DUPLICATE KEY UPDATE name = VALUES(name)").update();

        // Blocking: the thread waits right here until the row comes back
        String blocking = jdbc.sql("SELECT name FROM person WHERE id = 1").query(String.class).single();
        System.out.println("JDBC  (thread " + Thread.currentThread().getName() + ") -> " + blocking);

        // Reactive: this line only BUILDS a pipeline; StepVerifier subscribes and runs it
        var reactive = r2dbc.sql("SELECT name FROM person WHERE id = :id")
                .bind("id", 1)
                .map(row -> row.get("name", String.class))
                .one()
                .doOnNext(n -> System.out.println("R2DBC (thread " + Thread.currentThread().getName() + ") -> " + n));

        StepVerifier.create(reactive).expectNext("Koushik").verifyComplete();
    }
}
