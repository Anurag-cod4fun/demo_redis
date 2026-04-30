package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@SpringBootApplication
@EnableRedisDocumentRepositories(basePackages = {"com.example.demo"})
public class DemoredisApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoredisApplication.class, args);
	}

}
