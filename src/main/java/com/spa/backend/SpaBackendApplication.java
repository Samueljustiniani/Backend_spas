package com.spa.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpaBackendApplication.class, args);
	}

}