package com.lms.courseservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.lms.courseservice", "com.cyberlearnix"})
public class CourseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourseServiceApplication.class, args);
	}

}
