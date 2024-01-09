package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

public class QuarkScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = ".views-row";
  private static final String AJAX_PROGRESS_SELECTOR = "div.ajax-progress-fullscreen";
  private static final String NEXT_PAGE_SELECTOR =
      "li.page-item.pager__item.pager__item--next > a.page-link";

  public QuarkScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(cruiseLineService, expeditionService, "Quark Expeditions");
  }

  public void scrape() {
    navigateTo(cruiseLine.getExpeditionWebsite());

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
    try {
      waitForInvisibilityOfElement(By.cssSelector(AJAX_PROGRESS_SELECTOR));
      waitForPresenceOfElement(By.cssSelector(EXPEDITION_SELECTOR));

      Document doc = Jsoup.parse(driver.getPageSource());
      Elements expeditionItems = doc.select(EXPEDITION_SELECTOR);

      for (Element item : expeditionItems) {
        String website = extractWebsite(item);
        String name = extractName(item);
        String description = extractDescription(item);
        String port = extractPort(item);
        String duration = extractDuration(item);
        BigDecimal price = extractPrice(item);
        String photoUrl = extractPhotoUrl(item);

        saveExpedition(website, name, description, port, duration, price, photoUrl);
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
    WebElement element = driver.findElement(By.cssSelector(NEXT_PAGE_SELECTOR));
    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);

    element.click();
  }

  private String extractWebsite(Element item) {
    return cruiseLine.getWebsite() + item.select("div.expedition__info > div > a").attr("href");
  }

  private String extractName(Element item) {
    return item.select("div.expedition__info > h2").text();
  }

  private String extractDescription(Element item) {
    return item.select("div.expedition__info > .expedition__body").text();
  }

  private String extractPort(Element item) {
    return item.select(".expedition__summary > section.summary-bar > div")
        .select(".detail--departing-from > ul > li")
        .text();
  }

  private String extractDuration(Element item) {
    return item.select(".detail--duration > .detail__value").get(0).text();
  }

  private BigDecimal extractPrice(Element item) {
    String startingPrice = item.select("span.detail__price").text().replaceAll("[^\\d.]", "");
    return startingPrice.isEmpty() ? null : new BigDecimal(startingPrice.replace(",", ""));
  }

  private String extractPhotoUrl(Element item) {
    return cruiseLine.getWebsite()
        + item.select("div.hero-banner__image > picture > img").attr("src");
  }

  private void saveExpedition(
      String website,
      String name,
      String description,
      String port,
      String duration,
      BigDecimal price,
      String photoUrl) {
    expeditionService.saveIfNotExist(
        cruiseLine, website, name, description, port, port, duration, price, photoUrl);
  }
}
