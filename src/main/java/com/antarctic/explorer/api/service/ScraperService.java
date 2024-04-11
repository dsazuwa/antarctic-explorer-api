package com.antarctic.explorer.api.service;

import com.antarctic.explorer.api.scraper.AuroraScraper;
import com.antarctic.explorer.api.scraper.HurtigrutenScraper;
import com.antarctic.explorer.api.scraper.LindbladScraper;
import com.antarctic.explorer.api.scraper.Scraper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ScraperService {

  private final CruiseLineService cruiseLineService;
  private final VesselService vesselService;
  private final ExpeditionService expeditionService;
  private final ItineraryService itineraryService;
  private final DepartureService departureService;
  private final ExtensionService extensionService;

  @Autowired
  public ScraperService(
      CruiseLineService cruiseLineService,
      VesselService vesselService,
      ExpeditionService expeditionService,
      ItineraryService itineraryService,
      DepartureService departureService,
      ExtensionService extensionService) {
    this.cruiseLineService = cruiseLineService;
    this.vesselService = vesselService;
    this.expeditionService = expeditionService;
    this.itineraryService = itineraryService;
    this.departureService = departureService;
    this.extensionService = extensionService;
  }

  @Async
  public void scrapeData() {
    List<Scraper> scrapers =
        new ArrayList<>(
            Arrays.asList(
                new AuroraScraper(
                    cruiseLineService,
                    vesselService,
                    expeditionService,
                    itineraryService,
                    departureService,
                    extensionService),
                new HurtigrutenScraper(
                    cruiseLineService,
                    vesselService,
                    expeditionService,
                    itineraryService,
                    departureService,
                    extensionService),
                new LindbladScraper(
                    cruiseLineService,
                    vesselService,
                    expeditionService,
                    itineraryService,
                    departureService,
                    extensionService)
                //  new PonantScraper(cruiseLineService, expeditionService),
                //  new QuarkScraper(cruiseLineService, expeditionService),
                //  new VikingScraper(cruiseLineService, expeditionService)
                ));

    scrapers.forEach(
        scraper -> {
          scraper.scrape();
          System.out.println(scraper.getClass().getSimpleName() + " finished scraping");
        });
  }
}
