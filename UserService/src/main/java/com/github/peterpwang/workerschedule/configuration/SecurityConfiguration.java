package com.github.peterpwang.workerschedule.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * Spring security configuration class
 * @author Pei Wang
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtConfig jwtConfig;

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
			.addFilterBefore(new JwtAuthorizationFilter(authenticationManager(), jwtConfig), AnonymousAuthenticationFilter.class)
			.authorizeRequests()
			.antMatchers("/**").permitAll();
			//.and()
			//.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()); // Support CSRF in cookies
			
	}
	
	@Bean
	public JwtConfig jwtConfig() {
        	return new JwtConfig();
	}
}