package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.entity.User;
import com.example.demo.service.ReactiveService;

import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Fully reactive: R2DBC end to end, no extra threads, no blocking.
 * Empty Mono from the service = not found -> switchIfEmpty turns it into a 404.
 */
@Tag(name = "2. Users - Reactive (R2DBC)")
@RestController
@RequestMapping("/api/reactive/users")
public class UserReactController {

	private final ReactiveService reactiveService;

	public UserReactController(ReactiveService reactiveService) {
		this.reactiveService = reactiveService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<User> saveUser(@RequestBody User user) {
		return reactiveService.saveUserR(user);
	}

	@GetMapping
	public Flux<User> getAllUsers() {
		return reactiveService.getAllUsersR();
	}

	@GetMapping("/{id}")
	public Mono<User> getUserById(@PathVariable Long id) {
		return reactiveService.getUserByIdR(id).switchIfEmpty(Mono.error(notFound(id)));
	}

	@PutMapping("/{id}")
	public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
		return reactiveService.updateUserR(id, user).switchIfEmpty(Mono.error(notFound(id)));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> deleteUser(@PathVariable Long id) {
		return reactiveService.deleteUserR(id)
				.flatMap(deleted -> deleted ? Mono.<Void>empty() : Mono.error(notFound(id)));
	}

	private static ResponseStatusException notFound(Long id) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with id " + id);
	}
}
