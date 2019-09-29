package com.watchers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = { "com.watchers" })
public class WatchersApplication {

	public static void main(String[] args)
	{
		SpringApplication.run(WatchersApplication.class, args);
	}

}
