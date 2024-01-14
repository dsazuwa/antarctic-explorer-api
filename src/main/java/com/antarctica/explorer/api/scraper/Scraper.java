package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Scraper {
  private final WebDriverWait wait;
  protected CruiseLine cruiseLine;
  protected ExpeditionService expeditionService;
  private WebDriver driver;

  public Scraper(
      CruiseLineService cruiseLineService, ExpeditionService expeditionService, String name) {
    initializeDriver();
    this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));

    this.cruiseLine =
        cruiseLineService
            .findByName(name)
            .orElseThrow(() -> new RuntimeException("CruiseLine \"" + name + "\" not found"));
    this.expeditionService = expeditionService;
  }

  public abstract void scrape();

  protected abstract String getCurrentPageText();

  public void restartDriver() {
    quitDriver();
    initializeDriver();
  }

  protected void initializeDriver() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    this.driver = new ChromeDriver(options);
  }

  protected void navigateTo(String website, Runnable waitFunction) {
    driver.get(website);
    waitFunction.run();
    System.out.println("Loaded website: " + website);
  }

  protected void navigateTo(String website, String waitForPresenceSelector) {
    driver.get(website);
    waitForPresenceOfElement(waitForPresenceSelector);
    System.out.println("Loaded website: " + website);
  }

  protected void waitForPresenceOfElement(String cssSelector) {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)));
  }

  protected void waitForInvisibilityOfElement(String cssSelector) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(cssSelector)));
  }

  protected void waitForPageChange(String prevPage) {
    wait.until(
        (WebDriver wd) -> {
          String currentPage = getCurrentPageText();
          return !currentPage.equals(prevPage);
        });
  }

  protected String getCurrentUrl() {
    return driver.getCurrentUrl();
  }

  protected Document getParsedPageSource() {
    return Jsoup.parse(driver.getPageSource());
  }

  protected WebElement findElement(String cssSelector) {
    return driver.findElement(By.cssSelector(cssSelector));
  }

  protected List<WebElement> findElements(String cssSelector) {
    return driver.findElements(By.cssSelector(cssSelector));
  }

  protected void quitDriver() {
    if (driver != null) {
      driver.manage().deleteAllCookies();
      driver.quit();
    }
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

  protected BigDecimal extractPrice(Element element, String selector) {
    Elements priceElement = element.select(selector);
    if (priceElement.isEmpty()) return null;
    String priceText = priceElement.text().replaceAll("[^\\d.]", "");

    try {
      return new BigDecimal(priceText);
    } catch (NumberFormatException e) {
      System.err.println("Failed to parse price: " + priceText);
      return null;
    }
  }
}
