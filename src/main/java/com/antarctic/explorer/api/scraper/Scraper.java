package com.antarctic.explorer.api.scraper;

import com.antarctic.explorer.api.model.CruiseLine;
import com.antarctic.explorer.api.service.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Scraper {
  protected CruiseLine cruiseLine;

  protected CruiseLineService cruiseLineService;
  protected VesselService vesselService;
  protected ExpeditionService expeditionService;
  protected ItineraryService itineraryService;
  protected DepartureService departureService;
  protected ExtensionService extensionService;

  protected WebDriverWait wait;
  private WebDriver driver;

  public Scraper(
      CruiseLineService cruiseLineService,
      VesselService vesselService,
      ExpeditionService expeditionService,
      ItineraryService itineraryService,
      DepartureService departureService,
      ExtensionService extensionService,
      CruiseLine cruiseLine) {
    this.cruiseLine = cruiseLineService.saveIfNotExist(cruiseLine);

    this.cruiseLineService = cruiseLineService;
    this.vesselService = vesselService;
    this.expeditionService = expeditionService;
    this.itineraryService = itineraryService;
    this.departureService = departureService;
    this.extensionService = extensionService;

    initializeDriver();
  }

  public Scraper(
      CruiseLineService cruiseLineService,
      ExpeditionService expeditionService,
      String cruiseLineName,
      String cruiseLineWebsite,
      String expeditionWebsite,
      String cruiseLineLogo) {
    //    this.cruiseLine =
    //        cruiseLineService.saveIfNotExist(
    //            cruiseLineName, cruiseLineWebsite, expeditionWebsite, cruiseLineLogo);
    this.expeditionService = expeditionService;

    initializeDriver();
  }

  public abstract void scrape();

  protected abstract String getCurrentPageText();

  protected void initializeDriver() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments(
        "--headless",
        "--disable-gpu",
        "--disable-extensions",
        "--disable-infobars",
        "--disable-notifications",
        "--no-sandbox",
        "--disable-dev-shm-usage");
    this.driver = new ChromeDriver(options);
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));
  }

  public void quitDriver() {
    driver.quit();
  }

  protected JavascriptExecutor getExecutor() {
    return ((JavascriptExecutor) driver);
  }

  protected String getCurrentUrl() {
    return driver.getCurrentUrl();
  }

  protected Document getParsedPageSource() {
    return Jsoup.parse(driver.getPageSource());
  }

  protected WebElement findElement(By by) {
    return driver.findElement(by);
  }

  protected WebElement findElement(String cssSelector) {
    return driver.findElement(By.cssSelector(cssSelector));
  }

  protected List<WebElement> findElements(String cssSelector) {
    return driver.findElements(By.cssSelector(cssSelector));
  }

  protected WebElement findElement(WebElement element, String cssSelector) {
    return element.findElement(By.cssSelector(cssSelector));
  }

  protected List<WebElement> findElements(WebElement element, String cssSelector) {
    return element.findElements(By.cssSelector(cssSelector));
  }

  protected void navigateTo(String website, Runnable waitFunction) {
    driver.get(website);
    waitFunction.run();
    System.out.println("Loaded website: " + website);
  }

  protected void navigateTo(String website) {
    driver.get(website);
    System.out.println("Loaded website: " + website);
  }

  protected void navigateTo(String website, String waitForPresenceSelector) {
    driver.get(website);
    waitForPresenceOfElement(waitForPresenceSelector);
    System.out.println("Loaded website: " + website);
  }

  protected void navigateTo(String website, String[] waitForPresenceSelectors) {
    driver.get(website);
    for (String selector : waitForPresenceSelectors) waitForPresenceOfElement(selector);
    System.out.println("Loaded website: " + website);
  }

  protected String extractPhotoUrl(
      Element element, String selector, String attribute, String startPattern, String endPattern) {
    String attr = element.select(selector).attr(attribute);

    int startIndex = attr.indexOf(startPattern) + startPattern.length();
    int endIndex = attr.indexOf(endPattern, startIndex);

    if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex)
      throw new NoSuchElementException("Invalid/missing URL in element attribute");

    return attr.substring(startIndex, endIndex);
  }

  protected BigDecimal extractPrice(String price) {
    if (price.isEmpty()) return null;

    try {
      return new BigDecimal(price.replaceAll("[^\\d.]", ""));
    } catch (NumberFormatException e) {
      System.err.println("Failed to parse price: " + price);
      return null;
    }
  }

  protected BigDecimal extractPrice(Element element, String selector) {
    Elements priceElement = element.select(selector);
    return (priceElement.isEmpty()) ? null : extractPrice(priceElement.text());
  }

  /**
   * @param element the element to be scraped
   * @param startingPriceSelector starting price css selector
   * @param discountedPriceSelector discounted price css selector
   * @return an array of two prices. The first being the starting price and the second, the
   *     discounted price
   */
  protected BigDecimal[] extractPrices(
      Element element, String startingPriceSelector, String discountedPriceSelector) {
    BigDecimal startingPrice = extractPrice(element, startingPriceSelector);
    BigDecimal discountedPrice = extractPrice(element, discountedPriceSelector);

    return new BigDecimal[] {
      startingPrice == null ? discountedPrice : startingPrice,
      startingPrice == null ? null : discountedPrice
    };
  }

  protected void waitForInvisibilityOfElement(String cssSelector) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(cssSelector)));
  }

  protected void waitForPresenceOfElement(String cssSelector) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
  }

  protected void waitForPageChange(String prevPage) {
    wait.until(
        (WebDriver wd) -> {
          String currentPage = getCurrentPageText();
          return !currentPage.equals(prevPage);
        });
  }
}
