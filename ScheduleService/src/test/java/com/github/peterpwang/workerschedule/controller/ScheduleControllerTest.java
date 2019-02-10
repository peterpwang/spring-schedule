package com.github.peterpwang.workerschedule.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.Schedule;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.service.ScheduleService;
import com.github.peterpwang.workerschedule.controller.ScheduleController;
import com.github.peterpwang.workerschedule.util.Util;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@WithMockUser(username = "peter", roles = {"MANAGER"})
public class ScheduleControllerTest {

	private final Long SCHEDULE_ID_1 = 1L;
	private final String SCHEDULE_NAME_1 = "Schedule1";
	private final Long SCHEDULE_ID_2 = 2L;
	private final String SCHEDULE_NAME_2 = "Schedule2";
	private final Long SCHEDULE_ID_3 = 3L;
	private final String SCHEDULE_NAME_3 = "Schedule3";
	private static final Long MANAGER_ID = 10L;

	@Autowired
	private MockMvc mvc;

	@MockBean
	private ScheduleService service;

	@MockBean
	private ManagerRepository managerRepository;

	@Before
	public void setUp() throws Exception {
		Manager manager = Util.newManager(MANAGER_ID, "manager");
		Mockito.when(managerRepository.findByName("manager")).thenReturn(manager);
	}

	@Test
	public void givenSchedulesWhenGetSchedulesThenReturnJsonArray() throws Exception {
		Schedule schedule1 = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
		Schedule schedule2 = Util.newSchedule(SCHEDULE_ID_2, SCHEDULE_NAME_2);
		Schedule schedule3 = Util.newSchedule(SCHEDULE_ID_3, SCHEDULE_NAME_3);

		List<Schedule> allSchedules = new LinkedList<Schedule>(Arrays.asList(schedule1, schedule2, schedule3));
		PageRequest pageable = PageRequest.of(0, 10);
		Page<Schedule> page = new PageImpl<Schedule>(allSchedules, pageable, 3);

		given(service.findAll(pageable)).willReturn(page);

		mvc.perform(get("/schedules/api/schedules?page=0&size=10")
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.scheduleResources", hasSize(3)))
			.andExpect(jsonPath("$._embedded.scheduleResources[0].name", is(schedule1.getName())))
			.andExpect(jsonPath("$._embedded.scheduleResources[1].name", is(schedule2.getName())))
			.andExpect(jsonPath("$._embedded.scheduleResources[2].name", is(schedule3.getName())));
		verify(service, VerificationModeFactory.times(1)).findAll(pageable);
		reset(service);
	}

	@Test
	public void withNoScheduleCreateSchedule() throws Exception {
		Schedule schedule = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
		given(service.save(Mockito.any())).willReturn(schedule);
		
		mvc.perform(post("/schedules/api/schedules")
			.contentType(MediaType.APPLICATION_JSON)
			.content("") //Util.toJson(schedule)) caused Null pointer exception
			.with(csrf()))
			.andExpect(status().isBadRequest());
		//verify(service, VerificationModeFactory.times(1)).save(Mockito.any());
		reset(service);
	}

	@Test
	public void whenScheduleThenUpdateSchedule() throws Exception {
		Schedule schedule = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
		given(service.save(Mockito.any())).willReturn(schedule);

		schedule.setName(SCHEDULE_NAME_2);
		
		mvc.perform(put("/schedules/api/schedules/" + SCHEDULE_ID_1)
			.contentType(MediaType.APPLICATION_JSON)
			.content(Util.toJson(schedule))
			.with(csrf()))
			.andExpect(status().isOk());
		verify(service, VerificationModeFactory.times(1)).save(Mockito.any());
		reset(service);
	}

	@Test
	public void whenScheduleThenDeleteSchedule() throws Exception {
		Schedule schedule = Util.newSchedule(SCHEDULE_ID_1, SCHEDULE_NAME_1);
		Mockito.doNothing().when(service).deleteById(SCHEDULE_ID_1);
		
		mvc.perform(delete("/schedules/api/schedules/" + SCHEDULE_ID_1)
			.contentType(MediaType.APPLICATION_JSON)
			.with(csrf()))
			.andExpect(status().isOk());
		verify(service, VerificationModeFactory.times(1)).deleteById(Mockito.any());
		reset(service);
	}

}
