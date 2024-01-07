package com.antarctica.explorer.api;

import com.antarctica.explorer.api.service.ScraperService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AntarcticaExplorerApiApplication {

  public static void main(String[] args) {
    ConfigurableApplicationContext context =
        SpringApplication.run(AntarcticaExplorerApiApplication.class, args);

    ScraperService scrapeService = context.getBean(ScraperService.class);
    scrapeService.scrapeData();
  }
}
