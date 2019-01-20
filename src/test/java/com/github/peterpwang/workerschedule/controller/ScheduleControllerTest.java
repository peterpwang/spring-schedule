package com.github.peterpwang.workerschedule.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.service.ScheduleService;
import com.github.peterpwang.workerschedule.service.SpringDataJpaUserDetailsService;
import com.github.peterpwang.workerschedule.util.Util;

@RunWith(SpringRunner.class)
@WebMvcTest(ScheduleController.class)
public class ScheduleControllerTest {

	private final Long SCHEDULE_ID_1 = 1L;
	private final String SCHEDULE_NAME_1 = "Schedule1";
	private final Long SCHEDULE_ID_2 = 2L;
	private final String SCHEDULE_NAME_2 = "Schedule2";
	private final Long SCHEDULE_ID_3 = 3L;
	private final String SCHEDULE_NAME_3 = "Schedule3";
	private final Long SCHEDULE_ID_WRONG = 1000L;
	private static final Long MANAGER_ID = 10L;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ScheduleService service;
	
	@MockBean
	private SpringDataJpaUserDetailsService userDetailsService;

	@MockBean
	private ManagerRepository managerRepository;

    @Before
    public void setUp() throws Exception {
		Manager manager = Util.newManager(MANAGER_ID, "manager");
        Mockito.when(managerRepository.findByName("manager")).thenReturn(manager);
    }

    @Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
    public void whenPostSchedule_thenCreateSchedule() throws Exception {
        Schedule schedule = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
        given(service.save(Mockito.any())).willReturn(schedule);

        mvc.perform(post("/api/schedules").contentType(MediaType.APPLICATION_JSON).content(Util.toJson(schedule)).with(csrf()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is(SCHEDULE_NAME_1)));
        verify(service, VerificationModeFactory.times(1)).save(Mockito.any());
        reset(service);
    }

    @Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
    public void givenSchedules_whenGetSchedules_thenReturnJsonArray() throws Exception {
        Schedule schedule1 = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
        Schedule schedule2 = Util.newSchedule(SCHEDULE_ID_2, SCHEDULE_NAME_2);
        Schedule schedule3 = Util.newSchedule(SCHEDULE_ID_3, SCHEDULE_NAME_3);

		List<Schedule> allSchedules = new LinkedList<Schedule>(Arrays.asList(schedule1, schedule2, schedule3));
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Schedule> page = new PageImpl<Schedule>(allSchedules, pageable, 3);

        given(service.findAll(pageable)).willReturn(page);

        mvc.perform(get("/api/schedules?page=0&size=10").
			contentType(MediaType.APPLICATION_JSON)).
			andExpect(status().isOk()).
			andExpect(jsonPath("$._embedded.scheduleResources", hasSize(3))).
			andExpect(jsonPath("$._embedded.scheduleResources[0].name", is(schedule1.getName()))).
			andExpect(jsonPath("$._embedded.scheduleResources[1].name", is(schedule2.getName()))).
            andExpect(jsonPath("$._embedded.scheduleResources[2].name", is(schedule3.getName())));
        verify(service, VerificationModeFactory.times(1)).findAll(pageable);
        reset(service);
    }

}
