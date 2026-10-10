package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * One class used by BOTH JPA (blocking) and R2DBC (reactive).
 * Each library only reads its own annotations, so @Table and @Id appear twice:
 *   jakarta.persistence.*          -> JPA / Hibernate
 *   org.springframework.data.*     -> Spring Data R2DBC
 * The table itself is created by src/main/resources/schema.sql.
 */
@Entity
@Table(name = "users")
@org.springframework.data.relational.core.mapping.Table("users")
public class User {

	@Id
	@org.springframework.data.annotation.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)   // MySQL AUTO_INCREMENT generates the id
	private Long id;
	private String name;
	private String email;
	private String password;
	private String role;

	/** PUT semantics: replace every editable field with the incoming values (id is never copied). */
	public void updateFrom(User other) {
		this.name = other.name;
		this.email = other.email;
		this.password = other.password;
		this.role = other.role;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
