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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class QuarkScraper implements Scraper {
  private final WebDriver driver;
  private final CruiseLine cruiseLine;
  private final ExpeditionService expeditionService;

  public QuarkScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    System.setProperty("webdriver.chrome.driver", "C:\\dev\\tools\\chromedriver\\chromedriver.exe");
    this.driver = new ChromeDriver();
    this.cruiseLine =
        cruiseLineService
            .findByName("Quark Expeditions")
            .orElseThrow(
                () -> new RuntimeException(("CruiseLine \"Quark Expeditions\" not found")));
    this.expeditionService = expeditionService;
  }

  public void scrape() {
    driver.get(cruiseLine.getExpeditionWebsite());

    try {
      scrapeData();

      while (hasNextPage()) {
        navigateToNextPage();
        scrapeData();
      }
    } finally {
      quitDriver();
    }
  }

  private void scrapeData() {
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    String loaderSelector = "div.ajax-progress-fullscreen";
    String expeditionSelector = ".views-row";

    try {
      wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(loaderSelector)));
      wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(expeditionSelector)));

      Document doc = Jsoup.parse(driver.getPageSource());
      Elements expeditionItems = doc.select(expeditionSelector);

      for (Element item : expeditionItems) {
        String website =
            cruiseLine.getWebsite() + item.select("div.expedition__info > div > a").attr("href");
        String name = item.select("div.expedition__info > h2").text();
        String description = item.select("div.expedition__info > .expedition__body").text();

        Elements summary = item.select(".expedition__summary  > section.summary-bar > div");
        String port = summary.select(".detail--departing-from > ul > li").text();
        String duration = item.select(".detail--duration > .detail__value").get(0).text();
        String startingPrice =
            summary.select("span.detail__price").text().replaceAll("[^\\d.]", "");
        BigDecimal price =
            startingPrice.isEmpty() ? null : new BigDecimal(startingPrice.replace(",", ""));
        String photoUrl =
            cruiseLine.getWebsite() + item.select("div.hero-banner__image > picture > img").attr("src");

        expeditionService.saveIfNotExist(
            cruiseLine, website, name, description, port, port, duration, price, photoUrl);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private boolean hasNextPage() {
    List<WebElement> pages = driver.findElements(By.cssSelector("ul.pagination > li"));

    for (int i = 0; i < pages.size(); i++) {
      WebElement page = pages.get(i);
      String classes = page.getAttribute("class");

      if (classes != null && classes.contains("is-active") && classes.contains("active"))
        return i < pages.size() - 1;
    }

    return false;
  }

  private void navigateToNextPage() {
    String selector = "li.page-item.pager__item.pager__item--next > a.page-link";
    WebElement element = driver.findElement(By.cssSelector(selector));
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);

    element.click();
  }

  private void quitDriver() {
    this.driver.quit();
  }
}
