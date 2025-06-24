package com.commentarium;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import com.commentarium.controllers.auth.RegisterRequest;
import com.commentarium.services.AuthenticationService;

import static com.commentarium.entities.Role.ADMIN;

@SpringBootApplication
public class CommentariumApplication {

	@Value("${spring.application.admin.email}")
	private String adminEmail;

	@Value("${spring.application.admin.password}")
	private String adminPassword;

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(CommentariumApplication.class, args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (context != null) {
				context.close();
			}
		}));
	}

	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstName("Admin")
					.lastName("Admin")
					.email(adminEmail)
					.password(adminPassword)
					.username("Admin")
					.role(ADMIN)
					.build();
			System.out.println("Admin token: " + service.register(admin).getToken());

		};

	}
}
