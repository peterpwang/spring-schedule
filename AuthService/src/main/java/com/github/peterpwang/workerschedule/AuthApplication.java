package com.github.peterpwang.workerschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Spring Boot entry class
 * @author Pei Wang
 */
@SpringBootApplication
@EnableEurekaClient 		// It acts as a eureka client
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}
}
