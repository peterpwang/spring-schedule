package com.github.peterpwang.workerschedule.configuration;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.anyOf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.Filter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.peterpwang.workerschedule.UserApplication;
import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.domain.User;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.repository.UserRepository;
import com.github.peterpwang.workerschedule.util.Util;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = UserApplication.class)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class UserIntegrationTest {

	private final String MANAGER_NAME = "peter";
	private final String USER_NAME_1 = "andrew";
	private final String USER_NAME_2 = "john";
 
    @Autowired
    private MockMvc mvc;
 
    @Autowired
    private ManagerRepository managerRepository;
 
    @Autowired
    private UserRepository userRepository;
 
	@Before
	public void setUp() throws Exception {
		userRepository.deleteAll();
	}

	//@Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
	public void createUserThenStatus200() throws Exception {

		Manager manager = Util.newManager(MANAGER_NAME);
		Manager newManager = managerRepository.save(manager);
		User user = Util.newUser(USER_NAME_1, newManager);
		
		mvc.perform(post("/users/api/users")
		  .contentType(MediaType.APPLICATION_JSON)
		  .content(Util.toJson(user))
		  .with(csrf()))
		  .andExpect(status().isOk())
		  .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
		  .andExpect(jsonPath("$.name", is(USER_NAME_1)));
	}

	@Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
	public void givenUsersWhenFindUsersThenStatus200() throws Exception {

		User user = createAndSaveUser(USER_NAME_1, MANAGER_NAME);
		
		mvc.perform(get("/users/api/users")
		  .contentType(MediaType.APPLICATION_JSON))
		  .andExpect(status().isOk())
		  .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
		  .andExpect(jsonPath("$._embedded.users[0].name", is(USER_NAME_1)));
	}

	@Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
	public void givenUsersWhenGetUserThenStatus200() throws Exception {

		User user = createAndSaveUser(USER_NAME_1, MANAGER_NAME);
	
		mvc.perform(get("/users/api/users/" + user.getId())
		  .contentType(MediaType.APPLICATION_JSON))
		  .andExpect(status().isOk())
		  .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
		  .andExpect(jsonPath("$.name", is(USER_NAME_1)));
	}

	//@Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
	public void givenUsersWhenUpdateUserThenStatus200() throws Exception {

		User user = createAndSaveUser(USER_NAME_1, MANAGER_NAME);
		user.setName(USER_NAME_2);

		mvc.perform(put("/users/api/users/" + user.getId())
		  .contentType(MediaType.APPLICATION_JSON)
		  .content(Util.toJson(user))
		  .with(csrf()))
		  .andExpect(status().isOk())
		  .andExpect(content().contentTypeCompatibleWith("application/hal+json"))
		  .andExpect(jsonPath("$.name", is(USER_NAME_2)));
	}

	@Test
	@WithMockUser(username = "peter", password = "pass", roles = "MANAGER")
	public void givenUsersWhenDeleteUserThenStatus200() throws Exception {

		User user = createAndSaveUser(USER_NAME_1, MANAGER_NAME);

		mvc.perform(delete("/users/api/users/" + user.getId())
		  .contentType(MediaType.APPLICATION_JSON)
		  .with(csrf()))
		  .andExpect(status().isNoContent())
		  .andExpect(content().contentTypeCompatibleWith("application/hal+json"));
	}
 
	@After
	public void clear() throws Exception {
		userRepository.deleteAll();
	}
	
	private User createAndSaveUser(String userName, String managerName)
	{
		Manager manager = Util.newManager(managerName);
		Manager savedManager = managerRepository.save(manager);
		User user = Util.newUser(userName, savedManager);
		User savedUser = userRepository.save(user);
		return savedUser;
	}
}