package com.github.peterpwang.workerschedule.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.User;

/**
 * Repository configuration class
 * @author Pei Wang
 *
 */
@Configuration
public class RepositoryConfiguration implements RepositoryRestConfigurer {

	/**
	 * Class export must include id for User and Manager classes
	 */
	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(User.class);
		config.exposeIdsFor(Manager.class);
	}
}