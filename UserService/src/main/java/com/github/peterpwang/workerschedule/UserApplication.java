package com.github.peterpwang.workerschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Spring Boot entry class
 * @author Pei Wang
 */
@SpringBootApplication
@EnableEurekaClient 	// Enable eureka client. It inherits from @EnableDiscoveryClient.
public class UserApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}
}
