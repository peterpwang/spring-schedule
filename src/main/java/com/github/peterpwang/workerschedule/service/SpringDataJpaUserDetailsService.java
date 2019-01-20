package com.github.peterpwang.workerschedule.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;

@Component
public class SpringDataJpaUserDetailsService implements UserDetailsService {

	private final ManagerRepository repository;

	@Autowired
	public SpringDataJpaUserDetailsService(ManagerRepository repository) {
		this.repository = repository;
	}

	@Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
		Manager manager = this.repository.findByName(name);
		
		if (manager == null) {
            throw new UsernameNotFoundException(name);
        }
		
		return new User(manager.getName(), manager.getPassword(),
				AuthorityUtils.createAuthorityList(manager.getRoles()));
	}

}