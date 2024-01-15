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
                new AuroraScraper(cruiseLineService, expeditionService),
                new HurtigrutenScraper(cruiseLineService, expeditionService),
                new LindbladScraper(cruiseLineService, expeditionService),
                new PonantScraper(cruiseLineService, expeditionService),
                new SeabournScraper(cruiseLineService, expeditionService),
                new QuarkScraper(cruiseLineService, expeditionService),
                new VikingScraper(cruiseLineService, expeditionService)));

    scrapers.forEach(this::scrapeWithRetry);
  }

  private void scrapeWithRetry(Scraper scraper) {
    String scraperName = scraper.getClass().getSimpleName();

    for (int attempt = 1; true; attempt++)
      try {
        scraper.scrape();
        System.out.println(scraperName + " finished scraping");
        break;
      } catch (RuntimeException e) {
        e.printStackTrace();

        if (attempt > MAX_RETRIES)
          System.err.println("Failed to scrape after " + MAX_RETRIES + " attempts");

        System.out.println("Retrying " + scraperName + " (attempt " + attempt + ")");
        scraper.restartDriver();
      }
  }
}
