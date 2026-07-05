package com.taskmanager.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:mysql://localhost:3306/task_management_db",
		"spring.datasource.username=root",
		"spring.datasource.password=prabhapounraj@07",
		"jwt.secret=mysecuresecretkey12345678901234567890abc"
})
class TaskmanagerApplicationTests {

	@Test
	void contextLoads() {

	}
}