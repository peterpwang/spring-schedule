package com.github.peterpwang.workerschedule.configuration;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

/**
 * JWT authentication filter class
 * @author Pei Wang
 *
 */
public class JwtTokenAuthenticationFilter extends  OncePerRequestFilter {
    
	private final JwtConfig jwtConfig;
	
	public JwtTokenAuthenticationFilter(JwtConfig jwtConfig) {
		this.jwtConfig = jwtConfig;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
				
		HttpServletResponseWrapper wrapper = new HttpServletResponseWrapper(response) {
			
            @Override
			public void addHeader(String name, String value)
			{
                super.addHeader(name, value);
                handle(name, value);
			}
			
            private void handle(String name, String value) {
				if (name != null && name.compareTo(jwtConfig.getHeader()) == 0) {
					String responseHeader = getHeader(jwtConfig.getHeader());
					if(responseHeader != null && responseHeader.startsWith(jwtConfig.getPrefix())) {
						// Log in succeed
						String token = responseHeader.replace(jwtConfig.getPrefix(), "");
						
						try {
							// Validate the token
							Claims claims = Jwts.parser()
									.setSigningKey(jwtConfig.getSecret().getBytes())
									.parseClaimsJws(token)
									.getBody();
							
							String subject = claims.getSubject();
							if(subject != null) {
								int index = subject.indexOf('|');
								// Record logged in user. Avoid calling itself.
								super.addHeader("Loggedinmanager", subject.substring(0, index));
							}
						} catch (Exception e) {
							System.out.println("Claims: " + e);
						}
					}
				}
			}
        };
		
		// 1. get the authentication header. Tokens are supposed to be passed in the authentication header
		String header = request.getHeader(jwtConfig.getHeader());

		// 2. validate the header and check the prefix
		if(header == null || !header.startsWith(jwtConfig.getPrefix())) {			
			chain.doFilter(request, wrapper);  		// If not valid, go to the next filter.
			return;
		}
		
		// If there is no token provided and hence the user won't be authenticated. 
		// It's Ok. Maybe the user accessing a public path or asking for a token.
		
		// All secured paths that needs a token are already defined and secured in config class.
		// And If user tried to access without access token, then he won't be authenticated and an exception will be thrown.
		
		// 3. Get the token
		String token = header.replace(jwtConfig.getPrefix(), "");
		
		try {	// exceptions might be thrown in creating the claims if for example the token is expired
			
			// 4. Validate the token
			Claims claims = Jwts.parser()
					.setSigningKey(jwtConfig.getSecret().getBytes())
					.parseClaimsJws(token)
					.getBody();
			
			String username = claims.getSubject();
			if(username != null) {
				@SuppressWarnings("unchecked")
				List<String> authorities = (List<String>) claims.get("authorities");
				
				// 5. Create auth object
				// UsernamePasswordAuthenticationToken: A built-in object, used by spring to represent the current authenticated / being authenticated user.
				// It needs a list of authorities, which has type of GrantedAuthority interface, where SimpleGrantedAuthority is an implementation of that interface
				 UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
								 username, null, authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
				 
				 // 6. Authenticate the user
				 // Now, user is authenticated
				 SecurityContextHolder.getContext().setAuthentication(auth);
			}
			
		} catch (Exception e) {
			// In case of failure. Make sure it's clear; so guarantee user won't be authenticated
			SecurityContextHolder.clearContext();
		}
		
		// go to the next filter in the filter chain
		chain.doFilter(request, wrapper);
	}

}