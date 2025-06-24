package com.commentarium;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CommentariumApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(CommentariumApplication.class, args);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (context != null) {
				context.close();
			}
		}));
	}

}
