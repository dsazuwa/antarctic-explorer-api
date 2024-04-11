package com.antarctic.explorer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestAntarcticExplorerApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AntarcticExplorerApiApplication::main).with(TestAntarcticExplorerApiApplication.class).run(args);
	}

}
