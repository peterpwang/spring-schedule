package com.github.peterpwang.workerschedule.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

	private @Id @GeneratedValue Long id;

	@NotNull(message = "{user.name.notNull}")
	@Size(min = 1, max = 60, message = "{user.name.size}")
	private String name;

	@NotNull(message = "{user.password.notNull}")
	@Size(min = 1, max = 60, message = "{user.password.size}")
	private String password;

	@Transient
	private String passwordRepeat;

	@Size(max = 60, message = "{user.description.size}")
	private String description;

	@PositiveOrZero(message = "{user.active.number}")
	private Integer active;

	private @Version @JsonIgnore Long version;

	private @ManyToOne Manager manager;

	private User() {
	}

	public User(Long id, String name, String password, String passwordRepeat, String description, Integer active,
			Long version, Manager manager) {
		this.id = id;
		this.name = name;
		this.password = password;
		this.passwordRepeat = passwordRepeat;
		this.description = description;
		this.active = active;
		this.version = version;
		this.manager = manager;
	}
}