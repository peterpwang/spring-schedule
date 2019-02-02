package com.github.peterpwang.workerschedule.configuration;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import com.github.peterpwang.workerschedule.domain.Manager;
import com.github.peterpwang.workerschedule.service.SpringDataJpaUserDetailsService;

/**
 * Spring security configuration class
 * @author Pei Wang
 *
 */
@EnableWebSecurity 	// Enable security config. This annotation denotes config for spring security.
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private SpringDataJpaUserDetailsService userDetailsService;

	@Autowired
	private JwtConfig jwtConfig;

	/**
	 * Provide user/password security service
	 */
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService)
				.passwordEncoder(Manager.PASSWORD_ENCODER);
	}
	
	/**
	 * Configure protected paths and login/logout URIs
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
		    // make sure we use stateless session; session won't be used to store user's state.
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			// handle an authorized attempts 
			.exceptionHandling().authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED))
				.and()
			// Add a filter to validate user credentials and add token in the response header
		    // What's the authenticationManager()? 
		    // An object provided by WebSecurityConfigurerAdapter, used to authenticate the user passing user's credentials
		    // The filter needs this auth manager to authenticate the user.
		    .addFilter(new JwtUsernameAndPasswordAuthenticationFilter(authenticationManager(), jwtConfig))	
			.authorizeRequests()
				// allow all POST requests
				.antMatchers(HttpMethod.POST, jwtConfig.getUri()).permitAll()
				// any other requests must be authenticated
				.anyRequest().authenticated();
	}
	
	@Bean
	public JwtConfig jwtConfig() {
        	return new JwtConfig();
	}
}