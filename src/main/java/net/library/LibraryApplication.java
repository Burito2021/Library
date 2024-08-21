package net.library;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@SpringBootApplication
public class LibraryApplication implements EnvironmentPostProcessor {

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		var currentTime = LocalDateTime.now().format(formatter);

		Map<String, Object> map = Map.of("banner.current.time", currentTime);
		environment.getPropertySources().addLast(new MapPropertySource("customBannerProps", map));
	}
}
