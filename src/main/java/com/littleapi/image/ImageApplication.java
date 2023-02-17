package com.littleapi.image;

import com.littleapi.image.service.ImageService;
import com.littleapi.image.service.StorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ImageApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ImageApplication.class);
		app.setDefaultProperties(Collections
				.singletonMap("server.port", "80"));
		app.run(args);

	}

	@Bean
	CommandLineRunner init(ImageService imageService) {
		return (args) -> {
			imageService.deleteAll();
			imageService.init();
		};
	}

}
