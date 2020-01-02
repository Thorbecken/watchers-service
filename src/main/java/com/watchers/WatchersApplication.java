package com.watchers;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@ComponentScan
@EnableScheduling
@SpringBootApplication
@EnableJpaRepositories(basePackages = { "com.watchers" })
public class WatchersApplication {

	public static void main(String[] args){
		new SpringApplicationBuilder(WatchersApplication.class).bannerMode(Banner.Mode.OFF).run(args);
	}

}
