package com.antarctica.explorer.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestAntarcticaExplorerApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(AntarcticaExplorerApiApplication::main).with(TestAntarcticaExplorerApiApplication.class).run(args);
	}

}
