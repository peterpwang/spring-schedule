package com.github.peterpwang.workerschedule.configuration;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.Filter;

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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import lombok.Data;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.github.peterpwang.workerschedule.configuration.JwtConfig;
import com.github.peterpwang.workerschedule.configuration.JwtUsernameAndPasswordAuthenticationFilter;
import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.repository.ManagerRepository;
import com.github.peterpwang.workerschedule.service.SpringDataJpaUserDetailsService;


@RunWith(SpringRunner.class)
@WebMvcTest
@WebAppConfiguration
@ContextConfiguration(classes = { TestSecurityConfiguration.class })
public class AuthFilterTest {

    private static final String MANAGER_NAME = "peter";
    private static final String MANAGER_PASSWORD = "pass";
    private static String EXPECTED_HEADER = "Authorization";

    private static UserCredentials userCredentials;
	private static User user;

    @Autowired
    // Attribute name very important
    private WebApplicationContext webApplicationContext;

    @MockBean // userDetailsService automatically mocked when used
    private SpringDataJpaUserDetailsService userDetailsService;

    private MockMvc mockMvc;

    @Before
    public void before() {
		// Log in user information
		userCredentials = new UserCredentials();
		userCredentials.setUsername(MANAGER_NAME);
		userCredentials.setPassword(MANAGER_PASSWORD);
		
		// User information from database
		user = new User(MANAGER_NAME, MANAGER_PASSWORD, AuthorityUtils.createAuthorityList("MANAGER"));
        // Stub to define the behaviour of the service when it is used
        Mockito.when(userDetailsService.loadUserByUsername(MANAGER_NAME)).thenReturn(user);

		AuthenticationManager authenticationManager = createAuthenticationManager();
		JwtConfig jwtConfig = createJwtConfig();
		Filter filter = new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager, jwtConfig);
				
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(filter).build();
    }

    @Test
    public void loginSuccess() throws Exception {

		String body = "{ \"username\": \"" + userCredentials.getUsername() + "\", " +
                            "\"password\": \"" + userCredentials.getPassword() + "\" }";
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/auth")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body);
        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists(EXPECTED_HEADER));
   }

    @Test
    public void loginWrongUserFail() throws Exception {

		String body = "{ \"username\": \"John\", " +
                            "\"password\": \"" + userCredentials.getPassword() + "\" }";
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/auth")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body);
        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isUnauthorized());
   }

    @Test
    public void loginWrongPasswordFail() throws Exception {

		String body = "{ \"username\": \"" + userCredentials.getUsername() + "\", " +
                            "\"password\": \"wrongpass\" }";
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/auth")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body);
        mockMvc.perform(requestBuilder).andExpect(MockMvcResultMatchers.status().isUnauthorized());
   }

	private AuthenticationManager createAuthenticationManager() {
		AuthenticationManager am = mock(AuthenticationManager.class);
		when(am.authenticate(any(Authentication.class))).thenAnswer(
				new Answer<Authentication>() {
					public Authentication answer(InvocationOnMock invocation) throws Throwable {
						String username = (String)((Authentication)invocation.getArguments()[0]).getPrincipal();
						String password = (String)((Authentication)invocation.getArguments()[0]).getCredentials();
						UserDetails user = userDetailsService.loadUserByUsername(username);
						if (user == null || username.compareTo(user.getUsername()) != 0 || password.compareTo(user.getPassword()) != 0){
							throw new BadCredentialsException("Bad user name or password.");
						}
						return (Authentication) invocation.getArguments()[0];
					}
				});

		return am;
	}
	
	private JwtConfig createJwtConfig() {
		JwtConfig jwtConfig = new JwtConfig();
		jwtConfig.setUri("/auth/**");
		jwtConfig.setHeader("Authorization");
		jwtConfig.setPrefix("Bearer");
		jwtConfig.setExpiration(24*60*60);
		jwtConfig.setSecret("JwtSecretKey");
		return jwtConfig;
	}
	
	// A (temporary) class just to represent the user credentials
	@Data
	private static class UserCredentials {
	    private String username;
	    private String password;
	}
}
