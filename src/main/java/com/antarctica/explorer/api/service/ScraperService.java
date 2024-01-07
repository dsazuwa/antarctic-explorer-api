package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.scraper.QuarkScraper;
import com.antarctica.explorer.api.scraper.Scraper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {
  private final CruiseLineService cruiseLineService;
  private final ExpeditionService expeditionService;

  @Autowired
  public ScraperService(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    this.cruiseLineService = cruiseLineService;
    this.expeditionService = expeditionService;
  }

  @Scheduled(cron = "0 0 0 */7 * ?")
  public void scrapeData() {
    List<Scraper> scrapers =
        new ArrayList<>(List.of(new QuarkScraper(cruiseLineService, expeditionService)));
    scrapers.forEach(Scraper::scrape);
  }
}
