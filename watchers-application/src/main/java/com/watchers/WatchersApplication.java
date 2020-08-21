package com.watchers;

import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.watchers")
@SpringBootApplication
public class WatchersApplication {

	public static void main(String[] args){
		new SpringApplicationBuilder(WatchersApplication.class).bannerMode(Banner.Mode.OFF).run(args);
	}
}
