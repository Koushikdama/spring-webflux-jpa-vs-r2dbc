package com.example.demo.repository.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.example.demo.entity.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * REACTIVE repository (R2DBC). Every method returns Mono (0..1) or Flux (0..N), never a plain User.
 * Already inherited: save() -> Mono<User>, findById() -> Mono<User>, findAll() -> Flux<User>,
 *                    deleteById() -> Mono<Void>.
 */
public interface UserDbReactive extends R2dbcRepository<User, Long> {

	Mono<User> findByEmail(String email);

	Flux<User> findByRole(String role);
}
