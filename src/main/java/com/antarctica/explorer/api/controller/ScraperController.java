package com.antarctica.explorer.api.controller;

import com.antarctica.explorer.api.response.BaseResponse;
import com.antarctica.explorer.api.service.ScraperService;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scrapers")
public class ScraperController {

  @Autowired private ScraperService scraperService;

  @PostMapping("/run")
  public ResponseEntity<BaseResponse> runScrapers() {
    scraperService.scrapeData();

    CompletableFuture.runAsync(() -> scraperService.scrapeData());

    return ResponseEntity.accepted().body(new BaseResponse("Scrapers execution initiated"));
  }
}
