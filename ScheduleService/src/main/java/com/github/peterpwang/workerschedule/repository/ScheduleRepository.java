package com.github.peterpwang.workerschedule.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.peterpwang.workerschedule.domain.Schedule;

/**
 * Schedule repository class
 * @author Pei Wang
 *
 */
@PreAuthorize("hasRole('ROLE_MANAGER')")
public interface ScheduleRepository extends PagingAndSortingRepository<Schedule, Long> {
	
	Page<Schedule> findByNameIgnoreCaseContaining(Pageable pageable, String name);
}