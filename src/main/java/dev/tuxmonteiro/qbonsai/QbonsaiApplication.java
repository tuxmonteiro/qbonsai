package dev.tuxmonteiro.qbonsai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@Slf4j
@SpringBootApplication
public class QbonsaiApplication {

	public static void main(String[] args) {
		SpringApplication.run(QbonsaiApplication.class, args);
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		log.info("ObjectMapper created");
		return new ObjectMapper();
	}
}
