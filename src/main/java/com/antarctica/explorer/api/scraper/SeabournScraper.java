package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.pojo.SeabournExpeditionTrip;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class SeabournScraper extends Scraper {
  private static final String CRUISE_LINE_NAME = "Seaborn Expeditions";
  private static final String CRUISE_LINE_WEBSITE = "https://www.seabourn.com";
  private static final String EXPEDITION_WEBSITE =
      "https://www.seabourn.com/en/find-a-cruise?destinationIds:(S)&cruiseType:(EXPEDITION)";

  private static final String EXPEDITION_SELECTOR = "div.search-results__card-wrapper";
  private static final String LOAD_SELECTOR = "div.loadspinner";
  private static final String PAGE_SELECTOR = "div.pagination > div > span.content > span";
  private static final String NEXT_PAGE_SELECTOR = "div.pagination > div > button.next";

  private static final String PHOTO_SELECTOR = "a.search-results__card-images > picture > source";
  private static final String NAME_SELECTOR = "h2.search-results__card-title";
  private static final String PORTS_SELECTOR =
      "div.search-results__card-main-section > div > div.search-results__card-block > span";
  private static final String SHIP_NAME_SELECTOR = "div.search-results__card-shipname > span";
  private static final String DATE_SELECTOR = "button.date-carousel__data";
  private static final String WEBSITE_SELECTOR =
      "div.search-results__card-button-wrapper > div > a.cmp-button";
  private static final String PRICE_SELECTOR = "div.search-results__card-currency";
  private static final String SOLD_OUT_SELECTOR = "div.search-results__card-sold-out-block";

  public SeabournScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(
        cruiseLineService,
        expeditionService,
        CRUISE_LINE_NAME,
        CRUISE_LINE_WEBSITE,
        EXPEDITION_WEBSITE);
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

      groupExpeditions(expeditions).forEach(this::processElements);
    } finally {
      quitDriver();
    }
  }

  @Override
  protected String getCurrentPageText() {
    return findElements(PAGE_SELECTOR).get(1).getText();
  }

  private boolean hasNextPage() {
    List<WebElement> parts = findElements(PAGE_SELECTOR);

    int currentPageEnd = Integer.parseInt(parts.get(1).getText());
    int totalCruises = Integer.parseInt(parts.get(3).getText());

    return currentPageEnd < totalCruises;
  }

  private void navigateToNextPage() {
    waitForPresenceOfElement(EXPEDITION_SELECTOR);
    findElement(NEXT_PAGE_SELECTOR).click();
    waitForInvisibilityOfElement(LOAD_SELECTOR);
    System.out.println("Loaded search results: Page " + getCurrentPageText());
  }

  private Elements scrapeExpeditions() {
    return getParsedPageSource().select(EXPEDITION_SELECTOR);
  }

  private Map<String, List<Element>> groupExpeditions(Elements expeditions) {
    Map<String, List<Element>> groupedExpeditions = new HashMap<>();

    expeditions.forEach(
        (expedition) -> {
          String name = expedition.select(NAME_SELECTOR).text().trim();
          groupedExpeditions.computeIfAbsent(name, k -> new ArrayList<>()).add(expedition);
        });

    return groupedExpeditions;
  }

  private void processElements(String name, List<Element> elements) {
    Element element = elements.get(0);

    String photoUrl =
        cruiseLine.getWebsite() + element.select(PHOTO_SELECTOR).get(1).attr("data-srcset");
    String duration = extractDuration(name);

    String[] ports = extractPorts(element);
    String departingFrom = ports[0];
    String arrivingAt = ports[1];

    List<SeabournExpeditionTrip> trips =
        elements.stream().map(this::extractTrip).filter(Objects::nonNull).toList();
    BigDecimal cheapestTrip =
        trips.stream()
            .map(SeabournExpeditionTrip::startingPrice)
            .min(BigDecimal::compareTo)
            .orElse(null);
    String website = trips.size() == 1 ? trips.get(0).website() : null;

    expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        null,
        departingFrom,
        arrivingAt,
        duration,
        cheapestTrip,
        photoUrl);

    //  navigateTo(website, "h2#title-ititnerary");
  }

  private String extractDuration(String name) {
    Matcher matcher = Pattern.compile("(\\d+)-Day").matcher(name);

    if (matcher.find()) {
      String numDays = matcher.group(1);
      return numDays + " days";
    } else {
      throw new NoSuchElementException("Duration not found");
    }
  }

  private String[] extractPorts(Element element) {
    String[] ports =
        element.select(PORTS_SELECTOR).stream().map(Element::text).toArray(String[]::new);

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }

  private SeabournExpeditionTrip extractTrip(Element element) {
    if (isSoldOut(element)) return null;

    String startingDate = element.select(DATE_SELECTOR).attr("data-depart-date");
    BigDecimal startingPrice = extractPrice(element, PRICE_SELECTOR);
    String shipName = element.select(SHIP_NAME_SELECTOR).get(1).text();
    String website = element.select(WEBSITE_SELECTOR).attr("href");

    return new SeabournExpeditionTrip(startingDate, startingPrice, shipName, website);
  }

  private boolean isSoldOut(Element element) {
    return !element.select(SOLD_OUT_SELECTOR).isEmpty();
  }
}
