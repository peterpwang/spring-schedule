package com.github.peterpwang.workerschedule.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.repository.ScheduleRepository;
import com.github.peterpwang.workerschedule.repository.UserRepository;

@Service
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	ScheduleRepository scheduleRepository;

	@Autowired
	UserRepository userRepository;

	@Override
	public Page<Schedule> findAll(Pageable pageable) {
		return scheduleRepository.findAll(pageable);
	}

	public Optional<Schedule> findById(Long id) {
		return scheduleRepository.findById(id);
	}

	public Schedule save(Schedule schedule) {
		User user = schedule.getUser();
		Optional<User> userReal = userRepository.findById(user.getId());
		if (userReal.isPresent())
			schedule.setUser(userReal.get());
		else
			schedule.setUser(null);

		return scheduleRepository.save(schedule);
	}

	public void deleteById(Long id) {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Optional<Schedule> schedule = scheduleRepository.findById(id);
		if (!schedule.isPresent()) {
			throw new IllegalArgumentException("Bad data: schedule not exists.");
		}
		if (schedule.get().getManager() == null || schedule.get().getManager().getName() == null
				|| schedule.get().getManager().getName().compareTo(name) != 0) {
			throw new IllegalArgumentException("Bad data: You can't edit other user's schedule.");
		}
		scheduleRepository.deleteById(id);
	}
}