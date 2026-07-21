package dev.floelly.activitytrackerapi;

import org.springframework.boot.SpringApplication;

public class TestActivitytrackerapiApplication {

	public static void main(String[] args) {
		SpringApplication.from(ActivitytrackerapiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
