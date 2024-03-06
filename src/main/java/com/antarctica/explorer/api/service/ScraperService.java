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

  private final CruiseLineService cruiseLineService;
  private final VesselService vesselService;
  private final ExpeditionService expeditionService;
  private final ItineraryService itineraryService;
  private final DepartureService departureService;

  @Autowired
  public ScraperService(
      CruiseLineService cruiseLineService,
      VesselService vesselService,
      ExpeditionService expeditionService,
      ItineraryService itineraryService,
      DepartureService departureService) {
    this.cruiseLineService = cruiseLineService;
    this.vesselService = vesselService;
    this.expeditionService = expeditionService;
    this.itineraryService = itineraryService;
    this.departureService = departureService;
  }

  @Scheduled(cron = "0 0 0 */7 * ?")
  public void scrapeData() {
    List<Scraper> scrapers =
        new ArrayList<>(
            Arrays.asList(
                new AuroraScraper(
                    cruiseLineService,
                    vesselService,
                    expeditionService,
                    itineraryService,
                    departureService),
                new HurtigrutenScraper(
                    cruiseLineService,
                    vesselService,
                    expeditionService,
                    itineraryService,
                    departureService),
                new LindbladScraper(
                    cruiseLineService,
                    vesselService,
                    expeditionService,
                    itineraryService,
                    departureService)
                //  new PonantScraper(cruiseLineService, expeditionService),
                //  new QuarkScraper(cruiseLineService, expeditionService),
                //  new VikingScraper(cruiseLineService, expeditionService)
                ));

    try {
      scrapers.forEach(
          scraper -> {
            scraper.scrape();
            System.out.println(scraper.getClass().getSimpleName() + " finished scraping");
          });
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
  }
}
