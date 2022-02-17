package com.kimcompay.projectjb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy 
@SpringBootApplication
public class ProjectjbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectjbApplication.class, args);
	}

}
