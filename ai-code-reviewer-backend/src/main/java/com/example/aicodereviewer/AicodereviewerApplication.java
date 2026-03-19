package com.example.aicodereviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AicodereviewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AicodereviewerApplication.class, args);
	}

}
