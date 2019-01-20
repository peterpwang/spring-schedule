package com.github.peterpwang.workerschedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.repository.UserRepository;

@Component
public class DatabaseLoader implements CommandLineRunner {

	private final UserRepository userRepository;
	private final ManagerRepository managerRepository;

	@Autowired
	public DatabaseLoader(UserRepository userRepository, ManagerRepository managerRepository) {
		this.userRepository = userRepository;
		this.managerRepository = managerRepository;
	}

	@Override
	public void run(String... strings) throws Exception {
		Manager peter = this.managerRepository.save(new Manager(0L, "peter", "pass", "ROLE_MANAGER"));

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("peter",
				"doesn't matter", AuthorityUtils.createAuthorityList("ROLE_MANAGER")));

		this.userRepository.save(new User(0L, "john", "pass", "pass", "John", 1, 0L, peter));

		SecurityContextHolder.clearContext();

	}
}