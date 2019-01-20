package com.github.peterpwang.workerschedule.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.repository.ScheduleRepository;
import com.github.peterpwang.workerschedule.repository.UserRepository;
import com.github.peterpwang.workerschedule.service.ScheduleService;
import com.github.peterpwang.workerschedule.util.Util;

@RunWith(SpringRunner.class)
public class ScheduleServiceImplTest {

    @TestConfiguration
    static class ScheduleServiceImplTestContextConfiguration {
        @Bean
        public ScheduleService scheduleService() {
            return new ScheduleServiceImpl();
        }
    }

	private final Long SCHEDULE_ID_1 = 1L;
	private final String SCHEDULE_NAME_1 = "Schedule1";
	private final Long SCHEDULE_ID_2 = 2L;
	private final String SCHEDULE_NAME_2 = "Schedule2";
	private final Long SCHEDULE_ID_3 = 3L;
	private final String SCHEDULE_NAME_3 = "Schedule3";
	private final Long SCHEDULE_ID_WRONG = 1000L;
	private static final Long MANAGER_ID = 10L;
	private static final Long USER_ID = 100L;

    @Autowired
    private ScheduleService scheduleService;

    @MockBean
    private ManagerRepository managerRepository;

    @MockBean
    private ScheduleRepository scheduleRepository;

    @MockBean
    private UserRepository userRepository;

    @Before
    public void setUp() {
        Schedule schedule1 = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
        Schedule schedule2 = Util.newSchedule(SCHEDULE_ID_2, SCHEDULE_NAME_2);
        Schedule schedule3 = Util.newSchedule(SCHEDULE_ID_3, SCHEDULE_NAME_3);

        List<Schedule> allSchedules = Arrays.asList(schedule1, schedule2, schedule3);
		PageRequest pageable = PageRequest.of(0, 10);
		PageImpl<Schedule> page = new PageImpl<Schedule>(allSchedules, pageable, 3);

        Mockito.when(scheduleRepository.findById(SCHEDULE_ID_1)).thenReturn(Optional.of(schedule1));
        Mockito.when(scheduleRepository.findById(SCHEDULE_ID_2)).thenReturn(Optional.of(schedule2));
        Mockito.when(scheduleRepository.findById(SCHEDULE_ID_WRONG)).thenReturn(Optional.empty());
        Mockito.when(scheduleRepository.findAll(pageable)).thenReturn(page);
		
		Manager manager = Util.newManager(MANAGER_ID, "manager");
        Mockito.when(managerRepository.save(manager)).thenReturn(manager);
        Mockito.when(managerRepository.findByName("manager")).thenReturn(manager);

		User user = Util.newUser(USER_ID, "user");
        Mockito.when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @Test
    public void whenValidId_thenScheduleShouldBeFound() {
        Optional<Schedule> fromDb = scheduleService.findById(SCHEDULE_ID_1);
        assertThat(fromDb.isPresent()).isEqualTo(true);
        assertThat(fromDb.get().getName()).isEqualTo(SCHEDULE_NAME_1);

        verifyFindByIdIsCalledOnce();
    }

    @Test
    public void whenInValidId_thenScheduleShouldNotBeFound() {
        Optional<Schedule> fromDb = scheduleService.findById(SCHEDULE_ID_WRONG);
        verifyFindByIdIsCalledOnce();
        assertThat(fromDb.isPresent()).isEqualTo(false);
    }

    @Test
    public void given3Schedules_whengetAll_thenReturn3Records() {
        Schedule schedule1 = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
        Schedule schedule2 = Util.newSchedule(SCHEDULE_ID_2, SCHEDULE_NAME_2);
        Schedule schedule3 = Util.newSchedule(SCHEDULE_ID_3, SCHEDULE_NAME_3);

		PageRequest pagable = PageRequest.of(0, 10);
        Page<Schedule> allSchedules = scheduleService.findAll(pagable);
        assertThat(allSchedules.hasContent()).isEqualTo(true);

		List<Schedule> list = allSchedules.getContent();
        verifyFindAllSchedulesIsCalledOnce();
		
        assertThat(list).hasSize(3).extracting(Schedule::getName).contains(schedule1.getName(), schedule2.getName(), schedule3.getName());
    }

    private void verifyFindByIdIsCalledOnce() {
        Mockito.verify(scheduleRepository, VerificationModeFactory.times(1)).findById(Mockito.anyLong());
        Mockito.reset(scheduleRepository);
    }

    private void verifyFindAllSchedulesIsCalledOnce() {
		PageRequest pagable = PageRequest.of(0, 10);
        Mockito.verify(scheduleRepository, VerificationModeFactory.times(1)).findAll(pagable);
        Mockito.reset(scheduleRepository);
    }
}
