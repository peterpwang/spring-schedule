package com.github.peterpwang.workerschedule.configuration;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration class
 * @author Pei Wang
 *
 */
@EnableWebSecurity 	// Enable security config. This annotation denotes config for spring security.
public class SecurityTokenConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	private JwtConfig jwtConfig;
 
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
		   // Add a filter to validate the tokens with every request
		   .addFilterAfter(new JwtTokenAuthenticationFilter(jwtConfig), UsernamePasswordAuthenticationFilter.class)
		// authorization requests config
		.authorizeRequests()
			.antMatchers(HttpMethod.GET, "/built/**", "/styles/**", "/users/ws/**", "/scripts/**", "/", "/favicon.ico").permitAll()
		    // allow all who are accessing "auth" service
		    .antMatchers(jwtConfig.getUri()).permitAll()  
		    // must be an admin if trying to access admin area (authentication is also required here)
		    .antMatchers("/schedules/**").hasRole("MANAGER")
		    // Any other request must be authenticated
		    .anyRequest().authenticated()
		.and()
			.formLogin()
				.loginPage("/index")
				.failureUrl("/index")
				.defaultSuccessUrl("/", true)
				.permitAll()
		.and()
			.logout()
				.logoutSuccessUrl("/");
	}
	
	@Bean
  	public JwtConfig jwtConfig() {
    	   return new JwtConfig();
  	}
}