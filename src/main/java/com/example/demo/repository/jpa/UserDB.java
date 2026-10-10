package com.example.demo.repository.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

/*
 * BLOCKING repository (JPA / Hibernate over JDBC).
 * Already inherited, do NOT redeclare: save(), findById(), findAll(), existsById(), deleteById().
 * Custom methods must follow Spring Data naming: findBy..., existsBy..., countBy..., deleteBy...
 */
public interface UserDB extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);
}
