package com.github.peterpwang.workerschedule.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.security.access.prepost.PreAuthorize;

import com.github.peterpwang.workerschedule.domain.Schedule;

@PreAuthorize("hasRole('ROLE_MANAGER')")
public interface ScheduleRepository extends PagingAndSortingRepository<Schedule, Long> {
}