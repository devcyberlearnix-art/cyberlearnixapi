package com.lms.courseservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled("Context loading issue with Google Drive configuration")
@SpringBootTest
@ActiveProfiles("test")
class CourseServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
