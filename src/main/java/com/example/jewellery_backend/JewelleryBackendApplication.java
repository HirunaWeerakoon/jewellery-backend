package com.example.jewellery_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.jewellery_backend.config.FileStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class JewelleryBackendApplication {

	public static void main(String[] args) {
        SpringApplication.run(JewelleryBackendApplication.class, args);
	}

}
