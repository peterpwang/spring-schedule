package com.github.peterpwang.workerschedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;

/**
 * Database loader for initial data
 * @author Pei Wang
 *
 */
@Component
public class DatabaseLoader implements CommandLineRunner {

	private final ManagerRepository managerRepository;

	@Autowired
	public DatabaseLoader(ManagerRepository managerRepository) {
		this.managerRepository = managerRepository;
	}

	/**
	 * Create manager data
	 */
	@Override
	public void run(String... strings) throws Exception {
		this.managerRepository.save(new Manager(0L, "peter", "pass", "ROLE_MANAGER"));

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("peter",
				"doesn't matter", AuthorityUtils.createAuthorityList("ROLE_MANAGER")));

		SecurityContextHolder.clearContext();
	}
}