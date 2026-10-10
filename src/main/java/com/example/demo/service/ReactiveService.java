package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.r2dbc.UserDbReactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * REACTIVE service. Nothing here touches the database when the method is called —
 * each method only BUILDS a pipeline. WebFlux subscribes to it when it writes the HTTP response.
 * An empty Mono means "not found"; the controller turns that into a 404.
 */
@Service
public class ReactiveService {

	private final UserDbReactive userDbReactive;

	public ReactiveService(UserDbReactive userDbReactive) {
		this.userDbReactive = userDbReactive;
	}

	public Mono<User> saveUserR(User user) {
		user.setId(null);                     // R2DBC: id == null -> INSERT, id != null -> UPDATE
		return userDbReactive.save(user);
	}

	public Flux<User> getAllUsersR() {
		return userDbReactive.findAll();
	}

	public Mono<User> getUserByIdR(Long id) {
		return userDbReactive.findById(id);
	}

	public Mono<User> updateUserR(Long id, User changes) {
		return userDbReactive.findById(id)
				.flatMap(existing -> {
					existing.updateFrom(changes);
					return userDbReactive.save(existing);
				});
	}

	/** Emits true if a user was deleted, false if the id did not exist. */
	public Mono<Boolean> deleteUserR(Long id) {
		return userDbReactive.existsById(id)
				.flatMap(exists -> exists
						? userDbReactive.deleteById(id).thenReturn(true)
						: Mono.just(false));
	}
}
