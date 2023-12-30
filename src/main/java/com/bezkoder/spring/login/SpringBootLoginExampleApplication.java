package com.bezkoder.spring.login;

import com.bezkoder.spring.login.models.Role;
import com.bezkoder.spring.login.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootLoginExampleApplication {

	public static void main(String[] args) {

		SpringApplication.run(SpringBootLoginExampleApplication.class, args);

	}
}
