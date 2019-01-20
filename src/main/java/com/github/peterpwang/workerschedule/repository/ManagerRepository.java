package com.github.peterpwang.workerschedule.repository;

import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import com.github.peterpwang.workerschedule.domain.Manager;

@RepositoryRestResource(exported = false)
public interface ManagerRepository extends Repository<Manager, Long> {
	
	Manager save(Manager manager);

	Manager findByName(String name);
}