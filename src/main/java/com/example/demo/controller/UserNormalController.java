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
import com.example.demo.service.NormalService;
import com.example.demo.service.ReactiveService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/*
 * Every operation exists twice, side by side:
 *   /api/users/...           -> REACTIVE  : ReactiveService -> R2DBC, runs on Netty threads, no waiting
 *   /api/users/blocking/...  -> BLOCKING  : NormalService   -> JPA, wrapped so it doesn't block Netty
 *
 * Every blocking JPA call is written as:
 *   Mono.fromCallable(() -> jpaCall())                 -> 1. run the JPA call LATER, when WebFlux subscribes
 *       .subscribeOn(Schedulers.boundedElastic())      -> 2. on a thread pool that is allowed to WAIT
 *                                                            (never on Netty's event-loop thread)
 */
@Tag(name = "Users - Reactive vs Blocking")
@RestController
@RequestMapping("/api/users")
public class UserNormalController {

	private final NormalService normalService;
	private final ReactiveService reactiveService;

	public UserNormalController(NormalService normalService, ReactiveService reactiveService) {
		this.normalService = normalService;
		this.reactiveService = reactiveService;
	}

	// ======================= CREATE =======================

	@Operation(summary = "Create user — REACTIVE (R2DBC)")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<User> saveUser(@RequestBody User user) {
		return reactiveService.saveUserR(user);
	}

	@Operation(summary = "Create user — BLOCKING (JPA)")
	@PostMapping("/blocking")
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<User> saveUserBlocking(@RequestBody User user) {
		return Mono.fromCallable(() -> normalService.saveUser(user))    // JPA save -> User
				.subscribeOn(Schedulers.boundedElastic());               // run it on the waiting pool
	}

	// ======================= READ ALL =======================

	@Operation(summary = "All users — REACTIVE (R2DBC)")
	@GetMapping
	public Flux<User> getAllUsers() {
		return reactiveService.getAllUsersR();
	}

	/*
	 * How .flatMapMany(list -> Flux.fromIterable(list)) works:
	 *
	 * Mono<List<User>>                      [User1, User2, User3]   <- one box holding a list
	 *         |
	 *         | (1) UNWRAP: flatMapMany takes the List out of the Mono
	 *         |             and gives it to the lambda as "list"
	 *         v
	 * list -> Flux.fromIterable(list)
	 *         |
	 *         | (2) WRAP: Flux.fromIterable wraps the list into a Flux
	 *         |           right away (nothing is sent yet)
	 *         v
	 *         | (3) ITERATE: when WebFlux subscribes, it goes through the list
	 *         |              and sends User1, then User2, then User3
	 *         v
	 * Flux<User>                            User1 -> User2 -> User3 -> complete
	 */
	@Operation(summary = "All users — BLOCKING (JPA)")
	@GetMapping("/blocking")
	public Flux<User> getAllUsersBlocking() {
		return Mono.fromCallable(() -> normalService.getAllUsers())     // JPA findAll -> ONE List<User>
				.subscribeOn(Schedulers.boundedElastic())                // run it on the waiting pool,it retrun Mono<List<User>>
				.flatMapMany(list -> Flux.fromIterable(list));           // unpack the List into users, one by one
	}

	// ======================= READ ONE =======================

	@Operation(summary = "User by id — REACTIVE (R2DBC)")
	@GetMapping("/{id}")
	public Mono<User> getUserById(@PathVariable Long id) {
		return reactiveService.getUserByIdR(id).switchIfEmpty(Mono.error(notFound(id)));
	}

	@Operation(summary = "User by id — BLOCKING (JPA)")
	@GetMapping("/blocking/{id}")
	public Mono<User> getUserByIdBlocking(@PathVariable Long id) {
		return Mono.fromCallable(() -> normalService.getUserById(id).orElseThrow(() -> notFound(id)))  // User, or 404
				.subscribeOn(Schedulers.boundedElastic());
	}

	// ======================= UPDATE =======================

	@Operation(summary = "Update user — REACTIVE (R2DBC)")
	@PutMapping("/{id}")
	public Mono<User> updateUser(@PathVariable Long id, @RequestBody User user) {
		return reactiveService.updateUserR(id, user).switchIfEmpty(Mono.error(notFound(id)));
	}

	@Operation(summary = "Update user — BLOCKING (JPA)")
	@PutMapping("/blocking/{id}")
	public Mono<User> updateUserBlocking(@PathVariable Long id, @RequestBody User user) {
		return Mono.fromCallable(() -> normalService.updateUser(id, user).orElseThrow(() -> notFound(id)))  // User, or 404
				.subscribeOn(Schedulers.boundedElastic());
	}

	// ======================= DELETE =======================

	@Operation(summary = "Delete user — REACTIVE (R2DBC)")
	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> deleteUser(@PathVariable Long id) {
		return reactiveService.deleteUserR(id)
				.flatMap(deleted -> deleted ? Mono.<Void>empty() : Mono.error(notFound(id)));
	}

	@Operation(summary = "Delete user — BLOCKING (JPA)")
	@DeleteMapping("/blocking/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public Mono<Void> deleteUserBlocking(@PathVariable Long id) {
		return Mono.fromCallable(() -> normalService.deleteUser(id))     // true = deleted, false = not found
				.subscribeOn(Schedulers.boundedElastic())
				.flatMap(deleted -> deleted ? Mono.<Void>empty() : Mono.error(notFound(id)));
	}

	// ======================= helpers =======================

	private static ResponseStatusException notFound(Long id) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "No user with id " + id);
	}
}
