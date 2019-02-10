package com.github.peterpwang.workerschedule.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.peterpwang.workerschedule.domain.Schedule;

/**
 * Schedule service interface
 * @author Pei Wang
 *
 */
@PreAuthorize("hasRole('ROLE_MANAGER')")
public interface ScheduleService {
	public Page<Schedule> findAll(Pageable pageable);

	public Page<Schedule> findByNameIgnoreCaseContaining(Pageable pageable, String name);

	public Optional<Schedule> findById(Long id);

	@PreAuthorize("#schedule?.manager == null or #schedule?.manager?.name == authentication?.principal?.username")
	public Schedule save(@Param("schedule") Schedule schedule);

	@PreAuthorize("@scheduleRepository.findById(#id)?.manager?.name == authentication?.principal?.username")
	public void deleteById(@Param("id") Long id);
}