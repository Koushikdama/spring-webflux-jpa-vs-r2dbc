package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.jpa.UserDB;

/*
 * Plain BLOCKING service — exactly how you'd write it in Spring MVC.
 * Every call here makes the calling thread wait for MySQL, so the controller must run it
 * on Schedulers.boundedElastic(), never directly on a Netty event-loop thread.
 */
@Service
public class NormalService {

	private final UserDB userDB;

	public NormalService(UserDB userDB) {
		this.userDB = userDB;
	}

	public User saveUser(User user) {                    
		return userDB.save(user);
	}

	public List<User> getAllUsers() {
		return userDB.findAll();
	}

	public Optional<User> getUserById(Long id) {
		return userDB.findById(id);
	}

	public Optional<User> updateUser(Long id, User changes) {
		return userDB.findById(id).map(existing -> {
			existing.updateFrom(changes);
			return userDB.save(existing);
		});
	}

	/** @return false if no user had that id */
	public boolean deleteUser(Long id) {
		if (!userDB.existsById(id)) {
			return false;
		}
		userDB.deleteById(id);
		return true;
	}
}
