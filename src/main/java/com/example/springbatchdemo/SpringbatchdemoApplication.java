package com.example.springbatchdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class SpringbatchdemoApplication {

	public static void main(String[] args) {
		// log.info("Args passed in - {}", args[0]);
		SpringApplication.run(SpringbatchdemoApplication.class, args);
	}

}
