package com.github.peterpwang.workerschedule.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.peterpwang.workerschedule.domain.User;

/**
 * User repository class
 * @author Pei Wang
 *
 */
@PreAuthorize("hasRole('ROLE_MANAGER')")
public interface UserRepository extends PagingAndSortingRepository<User, Long> {

	@Override
	User save(@Param("user") User user);

	@Override
	void deleteById(@Param("id") Long id);

	@Override
	void delete(@Param("user") User user);

}