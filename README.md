# spring-webflux-jpa-vs-r2dbc

Spring Boot 4 + WebFlux user API built **twice, side by side**:

- **Reactive** — R2DBC end to end, runs on Netty threads, nothing waits
- **Blocking** — JPA (Hibernate), wrapped in `Mono.fromCallable(...).subscribeOn(Schedulers.boundedElastic())`
  so the blocking call never runs on Netty's event loop

Both write to the same MySQL `users` table, so you can create a user with one and read it with the other.

## Stack

Spring Boot 4.1 · Java 25 · WebFlux (Reactor Netty) · Spring Data R2DBC · Spring Data JPA · plain JDBC (`JdbcClient`) · MySQL · springdoc Swagger UI

## Endpoints

| Operation | Reactive (R2DBC) | Blocking (JPA) |
|---|---|---|
| Create | `POST /api/users` | `POST /api/users/blocking` |
| All | `GET /api/users` | `GET /api/users/blocking` |
| By id | `GET /api/users/{id}` | `GET /api/users/blocking/{id}` |
| Update | `PUT /api/users/{id}` | `PUT /api/users/blocking/{id}` |
| Delete | `DELETE /api/users/{id}` | `DELETE /api/users/blocking/{id}` |

Swagger UI: http://localhost:8080/swagger-ui.html

## Run it

1. Have MySQL running on `localhost:3306` (the `reactive_practice` database is created automatically).
2. Copy `src/main/resources/secrets.properties.example` to `secrets.properties` in the same folder and put
   your MySQL password in it. That file is git-ignored. (Or set the `DB_PASSWORD` environment variable.)
3. Start the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Tables are created by `src/main/resources/schema.sql` on startup.

## Project layout

```
com.example.demo
├── config/       JdbcConfig (DataSource), RepositoryConfig (JPA vs R2DBC repository packages)
├── controller/   UserNormalController — every operation in a reactive + a blocking version (/api/users)
│                 UserReactController  — reactive-only version (/api/reactive/users)
├── service/      NormalService (JPA, blocking) · ReactiveService (R2DBC, Mono/Flux)
├── entity/       User — one class mapped for both JPA and R2DBC
└── repository/
    ├── jpa/      UserDB          extends JpaRepository
    └── r2dbc/    UserDbReactive  extends R2dbcRepository
```
