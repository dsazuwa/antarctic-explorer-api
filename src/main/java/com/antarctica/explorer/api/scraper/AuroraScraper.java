package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AuroraScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = "div.col-lg-4.py-3 > div.block-offer";
  private static final String NEXT_PAGE_SELECTOR = "div.wp-pagenavi > a.nextpostslink";

  public AuroraScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(cruiseLineService, expeditionService, "Aurora Expeditions");
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

      expeditions.forEach(this::processExpedition);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    } finally {
      quitDriver();
    }
  }

  private Elements scrapeExpeditions() {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(EXPEDITION_SELECTOR)));
    Document doc = Jsoup.parse(driver.getPageSource());
    return doc.select(EXPEDITION_SELECTOR);
  }

  private void processExpedition(Element item) {
    Elements title = item.select("h4.mb-2 > a");
    String name = title.text();
    String website = title.attr("href");

    String duration = item.select("div.col > p.font-weight-bold").text().toLowerCase();
    BigDecimal startingPrice = extractPrice(item);
    String photoUrl = extractPhotoUrl(item);

    navigateTo(website);
    Document doc = Jsoup.parse(driver.getPageSource());
    String description = extractDescription(doc);
    String[] ports = extractPorts(doc);

    expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        ports[0],
        ports[1],
        duration,
        startingPrice,
        photoUrl);
  }

  private BigDecimal extractPrice(Element item) {
    Elements x = item.select("div.col > p.price > span.price__value");
    return (x.isEmpty()) ? null : new BigDecimal(x.text().replaceAll("[^\\d.]", ""));
  }

  private String extractPhotoUrl(Element item) {
    String styleAttr = item.select("a > div.embed-responsive-item").attr("style");

    int startIndex = styleAttr.indexOf("url('") + 5;
    int endIndex = styleAttr.indexOf("')", startIndex);

    if (startIndex < 0 || endIndex < 0 || endIndex <= startIndex)
      throw new NoSuchElementException("Invalid/missing URL in style attribute");
    return styleAttr.substring(startIndex, endIndex);
  }

  private String extractDescription(Document doc) {
    String selector = "div.container > div.row.section > div > p";
    waitForPresenceOfElement(selector);
    Elements elements = doc.select(selector);

    if (elements.isEmpty()) throw new NoSuchElementException("Description element not found");
    return elements.text();
  }

  private String[] extractPorts(Document doc) {
    String selector = "div.inner-content > div.details > dl.clearfix > dd";
    waitForPresenceOfElement(selector);
    String[] ports = doc.select(selector).get(1).text().split(" - ");

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }

  private String getCurrentPageText() {
    return driver.findElement(By.cssSelector("div.wp-pagenavi > span.current")).getText();
  }

  private boolean hasNextPage() {
    return !driver.findElements(By.cssSelector(NEXT_PAGE_SELECTOR)).isEmpty();
  }

  private void navigateToNextPage() {
    String prevPage = getCurrentPageText();

    WebElement nextPage = driver.findElement(By.cssSelector(NEXT_PAGE_SELECTOR));
    String href = nextPage.getAttribute("href");

    navigateTo(href);
    waitForPageChange(prevPage);
  }

  private void waitForPageChange(String prevPage) {
    wait.until(
        (WebDriver wd) -> {
          String currentPage = getCurrentPageText();
          return !currentPage.equals(prevPage);
        });
  }
}
