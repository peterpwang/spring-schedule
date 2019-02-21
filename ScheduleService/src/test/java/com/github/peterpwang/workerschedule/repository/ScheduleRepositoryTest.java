package com.github.peterpwang.workerschedule.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.util.Util;

/**
 * Schedule repository test class
 * @author Pei Wang
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WithMockUser(username = "peter", roles = {"MANAGER"})
public class ScheduleRepositoryTest {

	private final Long SCHEDULE_ID = 1L;
	private final String SCHEDULE_NAME = "Schedule1";
	private final String NEW_SCHEDULE_NAME = "ScheduleNew";
	private final Long MANAGER_ID = 10L;
	private final String MANAGER_NAME = "peter";
	private final Long USER_ID = 100L;
	private final String USER_NAME = "user";

	@Autowired
	private ManagerRepository managerRepository;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	public void givenEmptyDBWhenFindByIdThenReturnEmptyOptional() {
		Optional<Schedule> foundSchedule = scheduleRepository.findById(SCHEDULE_ID);

		assertThat(foundSchedule.isPresent()).isEqualTo(false);
	}

	@Test
	public void givenScheduleInDBWhenFindByIdThenReturnOptionalWithSchedule() {

		Schedule createdSchedule = createScheduleInDB();

		Optional<Schedule> foundSchedule = scheduleRepository.findById(createdSchedule.getId());

		assertThat(foundSchedule.isPresent()).isEqualTo(true);

		assertThat(foundSchedule.get().getName()).isEqualTo(SCHEDULE_NAME);
	}

	@Test
	public void givenEmptyDBWhenFindAllByPageThenReturnEmptyPage() {
		PageRequest pagable = PageRequest.of(0, 10);
		Page<Schedule> foundSchedules = scheduleRepository.findAll(pagable);

		assertThat(foundSchedules.hasContent()).isEqualTo(false);
	}

	@Test
	public void givenScheduleInDBWhenFindAllThenReturnPageWithSchedule() {

		createScheduleInDB();

		PageRequest pagable = PageRequest.of(0, 10);
		Page<Schedule> foundSchedules = scheduleRepository.findAll(pagable);

		assertThat(foundSchedules.hasContent()).isEqualTo(true);

		List<Schedule> list = foundSchedules.getContent();
		assertThat(list.size()).isEqualTo(1);

		assertThat(list.get(0).getName()).isEqualTo(SCHEDULE_NAME);
	}

	@Test
	public void createScheduleInDBThenReturnSchedule() {
		
		Manager manager = Util.newManager(MANAGER_ID, MANAGER_NAME);
		Manager createdManager = managerRepository.save(manager);

		User user = Util.newUser(USER_ID, USER_NAME);
		user.setManager(createdManager);
		User createdUser = userRepository.save(user);

		Schedule schedule = Util.newSchedule(SCHEDULE_ID, SCHEDULE_NAME);
		schedule.setManager(createdManager);
		schedule.setUser(createdUser);
		Schedule savedSchedule = scheduleRepository.save(schedule);

		assertThat(savedSchedule.getName()).isEqualTo(SCHEDULE_NAME);
	}

	@Test
	public void givenScheduleInDBWhenUpdateThenReturnSchedule() {
		
		Schedule schedule = createScheduleInDB();

		schedule.setName(NEW_SCHEDULE_NAME);
		Schedule savedSchedule = scheduleRepository.save(schedule);

		assertThat(savedSchedule.getName()).isEqualTo(NEW_SCHEDULE_NAME);
	}

	@Test
	public void givenScheduleInDBWhenDeleteThenReturnVoid() {
		
		Schedule schedule = createScheduleInDB();
		scheduleRepository.delete(schedule);
		Optional<Schedule> foundSchedule = scheduleRepository.findById(schedule.getId());

		assertThat(foundSchedule.isPresent()).isEqualTo(false);
	}

	@After
	public void cleanUp() {
		scheduleRepository.deleteAll();
	}

	private Schedule createScheduleInDB() {
		Manager manager = Util.newManager(MANAGER_ID, MANAGER_NAME);
		Manager createdManager = managerRepository.save(manager);

		User user = Util.newUser(USER_ID, USER_NAME);
		user.setManager(createdManager);
		User createdUser = userRepository.save(user);

		Schedule schedule = Util.newSchedule(SCHEDULE_ID, SCHEDULE_NAME);
		schedule.setManager(createdManager);
		schedule.setUser(createdUser);
		Schedule createdSchedule = scheduleRepository.save(schedule);
		return createdSchedule;
	}
}