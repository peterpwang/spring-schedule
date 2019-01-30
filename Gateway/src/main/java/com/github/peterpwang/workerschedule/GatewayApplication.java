package com.github.peterpwang.workerschedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * Spring Boot entry class
 * @author Pei Wang
 */
@SpringBootApplication
@EnableEurekaClient 		// It acts as a eureka client
@EnableZuulProxy			// Enable Zuul
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}
}
