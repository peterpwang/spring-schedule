package com.github.peterpwang.workerschedule.configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Jwts;

/**
 * JWT authorization filter class
 * @author Pei Wang
 *
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
	
	private final JwtConfig jwtConfig;
	
    public JwtAuthorizationFilter(AuthenticationManager authManager, JwtConfig jwtConfig) {
        super(authManager);
		this.jwtConfig = jwtConfig;
    }
	
    @Override
    protected void doFilterInternal(HttpServletRequest req,
			HttpServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		String header = req.getHeader(jwtConfig.getHeader());
        if (header == null || !header.startsWith(jwtConfig.getPrefix())) {
            chain.doFilter(req, res);
            return;
        }
		
        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }
	
    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(jwtConfig.getHeader());
        if (token != null) {
            // parse the token.
            String subject = Jwts.parser()
                    .setSigningKey(jwtConfig.getSecret().getBytes())
                    .parseClaimsJws(token.replace(jwtConfig.getPrefix(), ""))
                    .getBody()
                    .getSubject();
            if (subject != null) {
				int index = subject.indexOf('|');
				String user = subject.substring(0, index);
				String roles = subject.substring(index+1);
				
				String[] roleArray = roles.split(",");
				List<SimpleGrantedAuthority> roleList = new ArrayList<SimpleGrantedAuthority>();
				for (String role: roleArray) {
					roleList.add(new SimpleGrantedAuthority(role));
				}
                return new UsernamePasswordAuthenticationToken(user, null, roleList);
            }
            return null;
        }
        return null;
    }
}