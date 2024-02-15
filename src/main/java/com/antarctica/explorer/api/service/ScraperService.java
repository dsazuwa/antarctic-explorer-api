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
  private final VesselService vesselService;
  private final ExpeditionService expeditionService;
  private final ItineraryService itineraryService;

  @Autowired
  public ScraperService(
      CruiseLineService cruiseLineService,
      VesselService vesselService,
      ExpeditionService expeditionService,
      ItineraryService itineraryService) {
    this.cruiseLineService = cruiseLineService;
    this.vesselService = vesselService;
    this.expeditionService = expeditionService;
    this.itineraryService = itineraryService;
  }

  @Scheduled(cron = "0 0 0 */7 * ?")
  public void scrapeData() {
    List<Scraper> scrapers =
        new ArrayList<>(
            Arrays.asList(
                new AuroraScraper(
                    cruiseLineService, vesselService, expeditionService, itineraryService)
                //                new HurtigrutenScraper(cruiseLineService, expeditionService),
//                new LindbladScraper(
//                    cruiseLineService, vesselService, expeditionService, itineraryService)
                //                ,
                //                new PonantScraper(cruiseLineService, expeditionService),
                //                new SeabournScraper(cruiseLineService, expeditionService),
                //                                new QuarkScraper(cruiseLineService,
                // expeditionService)
                //                ,
                //                new VikingScraper(cruiseLineService, expeditionService)
                //
                ));

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
        System.out.println(e.getMessage());
        e.printStackTrace();

        if (attempt > MAX_RETRIES) {
          System.err.println("Failed to scrape after " + MAX_RETRIES + " attempts");
          break;
        }

        System.out.println("Retrying " + scraperName + " (attempt " + attempt + ")");
        scraper.restartDriver();
      }
  }
}
