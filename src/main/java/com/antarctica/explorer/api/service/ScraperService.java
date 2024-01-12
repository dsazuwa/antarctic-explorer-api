package com.antarctica.explorer.api.service;

import com.antarctica.explorer.api.scraper.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {
  private static final int MAX_RETRIES = 3;

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
        new ArrayList<>(
            Arrays.asList(
                new LindbladScraper(cruiseLineService, expeditionService),
                new QuarkScraper(cruiseLineService, expeditionService),
                new VikingScraper(cruiseLineService, expeditionService),
                new AuroraScraper(cruiseLineService, expeditionService),
                new PonantScraper(cruiseLineService, expeditionService)));

    scrapers.forEach(this::scrapeWithRetry);
  }

  private void scrapeWithRetry(Scraper scraper) {
    for (int attempt = 0; true; attempt++)
      try {
        scraper.scrape();
        break;
      } catch (RuntimeException e) {
        System.out.println(e.getMessage());

        if (attempt == MAX_RETRIES - 1)
          throw new RuntimeException("Failed to scrape after " + MAX_RETRIES + " attempts", e);

        System.out.println("Retrying scraper (attempt " + attempt + ")");
      }
  }
}
