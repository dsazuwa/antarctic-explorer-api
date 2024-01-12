package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.pojo.PonantExpeditionTrip;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class PonantScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = "div > div.fiche_produit";
  private static final String COOKIE_SELECTOR = "span.didomi-continue-without-agreeing";

  private static final String CURRENT_PAGE_SELECTOR =
      "ul.items.pages-items > li.item.current > strong.ponant_pager__current_page_emphasis";
  private static final String NEXT_PAGE_SELECTOR = "ul.items.pages-items > li.item.pages-item-next";

  private static final String NAME_SELECTOR =
      "div.fiche_produit__content > div.fiche_produit__produit--nom > p > a";
  private static final String EXPEDITION_TITLE_SELECTOR =
      "h1.text__title.text__l.color__white.margin__bottom_5";
  private static final String NUM_DAYS_SELECTOR = ".header__croisiere__jours > div > span.text__l";
  private static final String PHOTO_URL_SELECTOR = "div.fiche_produit__visuel > a > div.front";
  private static final String PHOTO_URL_ATTR = "data-bg";
  private static final String WEBSITE_SELECTOR = "div.fiche_produit__visuel > a";
  private static final String DESCRIPTION_SELECTOR = "div.readmore.show_more__collapsed > p";

  private static final String OTHER_TRIPS_LINK_SELECTOR =
      "div.header__croisiere__dates > a.text__underline";
  private static final String MODAL_CONTAINER_SELECTOR = "div.colonne__alone.modal_othersdates";
  private static final String TABS_INSIDE_MODAL_SELECTOR =
      MODAL_CONTAINER_SELECTOR
          + "div.colonne__alone.modal_othersdates > div > div[role=\"tablist\"] > div[role=\"tab\"]";

  private static final String MAIN_TRIP_DATE_SELECTOR =
      "div.header__croisiere__dates > h3 > span.text__xs";
  private static final String MAIN_TRIP_PORTS_SELECTOR =
      "div.header__croisiere__titre > h2.text__s > strong";
  private static final String MAIN_TRIP_PRICE_SELECTOR =
      "div.header__croisiere__prix > div > span.price";
  private static final String MAIN_TRIP_SHIP_SELECTOR = "ul.menu__page_right > li.col-sm-12";

  private static final String OTHER_TRIP_DEPARTURE_SELECTOR = "div[data-label=\"Departure\"]";
  private static final String OTHER_TRIP_ARRIVAL_SELECTOR = "div[data-label=\"Arrival\"]";
  private static final String OTHER_TRIP_DATE_SELECTOR = "div[data-label=\"Dates\"]";
  private static final String OTHER_TRIP_PRICE_SELECTOR = "div > span.price";
  private static final String OTHER_TRIP_SHIP_SELECTOR = "div[data-label=\"Ships\"]";
  private static final String OTHER_TRIP_WEBSITE_SELECTOR = "div.row > div.row > a.button";

  private boolean cookieAccepted = false;

  public PonantScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(cruiseLineService, expeditionService, "Ponant");
  }

  @Override
  public void scrape() {
    navigateTo(cruiseLine.getExpeditionWebsite());

    try {
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

  private Elements scrapeExpeditions() {
    if (!cookieAccepted && isCookieElementVisible()) acceptCookie();
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(EXPEDITION_SELECTOR)));
    Document doc = Jsoup.parse(driver.getPageSource());
    return doc.select(EXPEDITION_SELECTOR);
  }

  private boolean isCookieElementVisible() {
    try {
      WebElement element = driver.findElement(By.cssSelector(COOKIE_SELECTOR));
      return element.isDisplayed();
    } catch (NoSuchElementException | StaleElementReferenceException e) {
      return false;
    }
  }

  private void acceptCookie() {
    WebElement acceptCookieButton = driver.findElement(By.cssSelector(COOKIE_SELECTOR));
    acceptCookieButton.click();
    cookieAccepted = true;
  }

  private String getCurrentPageText() {
    return driver.findElement(By.cssSelector(CURRENT_PAGE_SELECTOR)).getText();
  }

  private boolean hasNextPage() {
    return !driver.findElements(By.cssSelector(NEXT_PAGE_SELECTOR)).isEmpty();
  }

  private void navigateToNextPage() {
    String prevPage = getCurrentPageText();
    WebElement nextPageElement = driver.findElement(By.cssSelector(NEXT_PAGE_SELECTOR + " > a"));
    String href = nextPageElement.getAttribute("href");

    navigateTo(href);
    waitForPageChange(prevPage);
  }

  private Document navigateToExpedition(String website) {
    navigateTo(website);
    waitForPresenceOfElement(By.cssSelector(EXPEDITION_TITLE_SELECTOR));
    return Jsoup.parse(driver.getPageSource());
  }

  private void waitForPageChange(String prevPage) {
    wait.until(
        (WebDriver wd) -> {
          String currentPage = getCurrentPageText();
          return !currentPage.equals(prevPage);
        });
  }

  private Map<String, List<Element>> groupExpeditions(Elements expeditions) {
    Map<String, List<Element>> groupedExpeditions = new HashMap<>();

    expeditions.forEach(
        (expedition) -> {
          String name = expedition.select(NAME_SELECTOR).text();
          groupedExpeditions
              .computeIfAbsent(name.replaceAll("[’‘]", "'").trim(), k -> new ArrayList<>())
              .add(expedition);
        });

    return groupedExpeditions;
  }

  private void processElements(String name, List<Element> elements) {
    Element element = elements.get(0);
    String photoUrl = extractPhotoUrl(element);
    String website = cruiseLine.getWebsite() + element.select(WEBSITE_SELECTOR).attr("href");

    Document doc = navigateToExpedition(website);
    String duration = extractDuration(doc);
    String description = doc.select(DESCRIPTION_SELECTOR).text();
    List<PonantExpeditionTrip> trips = extractTrips(doc);

    String commonDepartingPort = getCommonPort(trips, PonantExpeditionTrip::departingFrom);
    String commonArrivalPort = getCommonPort(trips, PonantExpeditionTrip::arrivingAt);

    BigDecimal cheapestTrip =
        trips.stream()
            .map(PonantExpeditionTrip::startingPrice)
            .min(BigDecimal::compareTo)
            .orElse(null);

    expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        commonDepartingPort,
        commonArrivalPort,
        duration,
        cheapestTrip,
        photoUrl);
  }

  private List<PonantExpeditionTrip> extractTrips(Document doc) {
    List<PonantExpeditionTrip> trips = new ArrayList<>(List.of(extractMainTrip(doc)));

    try {
      WebElement link = driver.findElement(By.cssSelector(OTHER_TRIPS_LINK_SELECTOR));
      link.click();
      waitForPresenceOfElement(By.cssSelector(MODAL_CONTAINER_SELECTOR));

      trips.addAll(
          doc.select(TABS_INSIDE_MODAL_SELECTOR).stream().map(this::extractOtherTrip).toList());
    } catch (NoSuchElementException e) {
      System.out.println("No other dates found");
    }

    return trips;
  }

  private PonantExpeditionTrip extractMainTrip(Element element) {
    String[] ports = extractMainTripPorts(element);
    String departingFrom = ports[0];
    String arrivingAt = ports[1];

    String[] dates = extractDates(element.select(MAIN_TRIP_DATE_SELECTOR).get(1).text());
    String startDate = dates[0];
    String endDate = dates[1];

    BigDecimal startingPrice = extractPrice(element, MAIN_TRIP_PRICE_SELECTOR);
    String shipName = extractMainTripShip(element);
    element.select(MAIN_TRIP_SHIP_SELECTOR).text();

    return new PonantExpeditionTrip(
        departingFrom,
        arrivingAt,
        startDate,
        endDate,
        startingPrice,
        shipName,
        driver.getCurrentUrl());
  }

  private PonantExpeditionTrip extractOtherTrip(Element element) {
    String departingFrom = element.select(OTHER_TRIP_DEPARTURE_SELECTOR).text();
    String arrivingAt = element.select(OTHER_TRIP_ARRIVAL_SELECTOR).text();

    String[] dates = extractDates(element.select(OTHER_TRIP_DATE_SELECTOR).text());
    String startDate = dates[0];
    String endDate = dates[1];

    BigDecimal startingPrice = extractPrice(element, OTHER_TRIP_PRICE_SELECTOR);
    String shipName = element.select(OTHER_TRIP_SHIP_SELECTOR).text();
    String website = element.select(OTHER_TRIP_WEBSITE_SELECTOR).attr("href");

    return new PonantExpeditionTrip(
        departingFrom, arrivingAt, startDate, endDate, startingPrice, shipName, website);
  }

  private String extractPhotoUrl(Element element) {
    String attr = element.select(PHOTO_URL_SELECTOR).attr(PHOTO_URL_ATTR);

    int startIndex = attr.indexOf("url(") + 4;
    int endIndex = attr.indexOf(")", startIndex);

    if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex)
      throw new NoSuchElementException("Invalid/missing URL in element attribute");
    return attr.substring(startIndex, endIndex);
  }

  private String extractDuration(Document doc) {
    Element daysElement = doc.select(NUM_DAYS_SELECTOR).first();
    return daysElement.text() + " days";
  }

  private String extractMainTripShip(Element element) {
    Element shipElement = element.select(OTHER_TRIP_SHIP_SELECTOR).first();
    String shipText = shipElement != null ? shipElement.text() : "";
    return shipText.split("Your ship")[0].trim();
  }

  private String[] extractMainTripPorts(Element element) {
    String[] ports = element.select(MAIN_TRIP_PORTS_SELECTOR).text().split(" - ");

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }

  private String[] extractDates(String dateString) {
    String[] dates = dateString.split(" to ");

    if (dates.length != 2) throw new NoSuchElementException("Dates not found");
    return dates;
  }

  private String getCommonPort(
      List<PonantExpeditionTrip> trips, Function<PonantExpeditionTrip, String> portExtractor) {
    return trips.stream().map(portExtractor).distinct().count() == 1
        ? trips.get(0).arrivingAt()
        : null;
  }
}
