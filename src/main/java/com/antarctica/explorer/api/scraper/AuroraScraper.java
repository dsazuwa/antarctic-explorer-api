package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.model.Vessel;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import com.antarctica.explorer.api.service.VesselService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AuroraScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = "div.col-lg-4.py-3 > div.block-offer";
  private static final String CURRENT_PAGE_SELECTOR = "div.wp-pagenavi > span.current";
  private static final String NEXT_PAGE_SELECTOR = "div.wp-pagenavi > a.nextpostslink";

  public AuroraScraper(
      CruiseLineService cruiseLineService,
      VesselService vesselService,
      ExpeditionService expeditionService) {
    super(cruiseLineService, vesselService, expeditionService, "Aurora Expeditions");
  }

  @Override
  public void scrape() {
    try {
      navigateTo(cruiseLine.getExpeditionWebsite(), EXPEDITION_SELECTOR);
      Elements expeditions = scrapeExpeditions();

      while (hasNextPage()) {
        navigateToNextPage();
        expeditions.addAll(scrapeExpeditions());
      }

      expeditions.forEach(
          element -> {
            Expedition expedition = processExpedition(element);
            scrapeItinerary(expedition);
            scrapeDeparture(expedition);
          });
    } finally {
      quitDriver();
    }
  }

  @Override
  protected String getCurrentPageText() {
    return findElement(CURRENT_PAGE_SELECTOR).getText();
  }

  private boolean hasNextPage() {
    return !findElements(NEXT_PAGE_SELECTOR).isEmpty();
  }

  private void navigateToNextPage() {
    String prevPage = getCurrentPageText();
    String href = findElement(NEXT_PAGE_SELECTOR).getAttribute("href");
    navigateTo(href, () -> waitForPageChange(prevPage));
  }

  private Elements scrapeExpeditions() {
    return getParsedPageSource().select(EXPEDITION_SELECTOR);
  }

  private Expedition processExpedition(Element element) {
    String titleSelector = "h4.mb-2 > a";
    String durationSelector = "div.col > p.font-weight-bold";
    String photoSelector = "a > div.embed-responsive-item";
    String descriptionSelector = "div.container > div.row.section > div > p";
    String priceSelector = "div.col > p.price > span.price__value";

    Elements title = element.select(titleSelector);
    String name = title.text();
    String website = title.attr("href");

    String duration = element.select(durationSelector).text().replaceAll("[A-Za-z\\s]", "");
    BigDecimal startingPrice = extractPrice(element, priceSelector);
    String photoUrl = extractPhotoUrl(element, photoSelector, "style", "url('", "')");

    navigateTo(website);
    Document doc = getParsedPageSource();

    Elements descriptionElements = doc.select(descriptionSelector);
    String description =
        (descriptionElements.isEmpty())
            ? null
            : descriptionElements.stream()
                .map(Element::text)
                .collect(
                    StringBuilder::new,
                    (sb, text) -> sb.append(text).append("\n"),
                    StringBuilder::append)
                .toString();

    String[] highlights = extractHighlights(doc);
    String[] ports = extractPorts(doc);

    return expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        highlights,
        ports[0],
        ports[1],
        duration,
        startingPrice,
        photoUrl);
  }

  private void scrapeDeparture(Expedition expedition) {
    String optionSelector = "div.details > dl.clearfix > dd > select > option";
    String priceSelector = "dd > h4 > span.price__value";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    String mainWebsite = getCurrentUrl();

    getParsedPageSource()
        .select(optionSelector)
        .forEach(
            option -> {
              String[] dates = option.text().split(" - ");
              LocalDate startDate = LocalDate.parse(dates[0], formatter);
              LocalDate endDate = LocalDate.parse(dates[1], formatter);

              String name = option.attr("value");
              String website = mainWebsite + "?code=" + name;
              navigateTo(
                  website,
                  () ->
                      wait.until(
                          ExpectedConditions.textToBePresentInElement(
                              findElements("div.details > dl.clearfix > dd").get(2), name)));

              Document doc = getParsedPageSource();
              BigDecimal startingPrice = extractPrice(doc, priceSelector);
              if (startingPrice == null) return;
              String[] ports = extractPorts(doc);

              Vessel vessel = scrapeVessel(doc);
              if (vessel == null) {
                Optional<Vessel> randVessel = vesselService.findOneByCruiseLIne(cruiseLine);
                if (randVessel.isEmpty()) return;
                vessel = randVessel.get();
              }

              expeditionService.saveDeparture(
                  expedition,
                  vessel,
                  name,
                  ports[0],
                  ports[1],
                  startDate,
                  endDate,
                  startingPrice,
                  website);
            });
  }

  private void scrapeItinerary(Expedition expedition) {}

  private Vessel scrapeVessel(Document doc) {
    String shipSelector = "div.details > dl.clearfix > dd";
    String capacitySelector = "div > p > span.prop";
    String photoSelector = "div.col-xl-4.py-4 > p > img";

    Element shipElement = doc.select(shipSelector).get(4).selectFirst("a");
    if (shipElement == null) return null;
    String name = shipElement.text();
    String website = shipElement.attr("href");

    Optional<Vessel> vessel = vesselService.findByName(name);
    if (vessel.isPresent()) return vessel.get();

    navigateTo(website);
    Document shipDoc = getParsedPageSource();
    int capacity =
        Integer.parseInt(
            Objects.requireNonNull(shipDoc.selectFirst(capacitySelector))
                .text()
                .replaceAll("\\D", ""));
    String photoUrl = shipDoc.select(photoSelector).attr("src");

    return vesselService.saveIfNotExist(cruiseLine, name, capacity, website, photoUrl);
  }

  private String[] extractHighlights(Document doc) {
    String[] highlights =
        doc.select("div.container > div.section > div.col-xl-8 > div.section > p > span").stream()
            .map(x -> x.text().replace("•", "").replace(".", "").trim())
            .filter(x -> !x.isEmpty())
            .toArray(String[]::new);

    if (highlights.length != 0) return highlights;

    return doc.select("div.section > ul > li > span").stream()
        .map(Element::text)
        .toArray(String[]::new);
  }

  private String[] extractPorts(Document doc) {
    String portsSelector = "div.inner-content > div.details > dl.clearfix > dd";
    String[] ports = doc.select(portsSelector).get(1).text().split(" - ");

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }
}
