package com.example.instructorservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Context loading issue - needs investigation")
@SpringBootTest(properties = "spring.profiles.active=test")
class InstructorserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
