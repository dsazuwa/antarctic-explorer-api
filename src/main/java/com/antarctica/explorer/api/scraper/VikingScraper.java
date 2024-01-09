package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class VikingScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = "div.cruise-detail-wrapper > div.cruise-detail";
  private static final String DESCRIPTION_SELECTOR = ".hero-sidebar-content > div.description";

  public VikingScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(cruiseLineService, expeditionService, "Viking Expeditions");
  }

  public void scrape() {
    navigateTo(cruiseLine.getExpeditionWebsite());

    try {
      scrapeData();
    } finally {
      quitDriver();
    }
  }

  private void scrapeData() {
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(EXPEDITION_SELECTOR)));
    Document doc = Jsoup.parse(driver.getPageSource());
    Elements expeditionItems = doc.select(EXPEDITION_SELECTOR);

    for (Element item : expeditionItems) {
      Element titleElement =
          item.selectFirst("div.caption > section.title > div.detail > div.cruise-title-wrapper");

      Element titleButton = titleElement.selectFirst("a.cruise-link.button-link");
      String name = titleButton.selectFirst("h3.cruise-link").text();
      String website = cruiseLine.getWebsite() + titleButton.attr("href");

      String[] ports = titleElement.selectFirst("h4").text().split(" to ");
      String startingPort = ports.length > 0 ? ports[0] : null;
      String endingPort = ports.length > 1 ? ports[1] : null;

      Elements infoElement = item.select("section.info > div.detail > div");
      String duration = infoElement.get(0).select("div.item > span.value").text();
      String startingPrice =
          infoElement.get(2).select("div.item > span.value").text().replaceAll("[^\\d.]", "");

      String photoUrl = item.select("img.cruise-link.thumbnail-img").attr("src");

      String description = extractDescription(website);

      expeditionService.saveIfNotExist(
          cruiseLine,
          website,
          name,
          description,
          startingPort,
          endingPort,
          duration,
          new BigDecimal(startingPrice),
          photoUrl);
    }
  }

  private String extractDescription(String website) {
    navigateTo(website);
    Document doc = Jsoup.parse(driver.getPageSource());
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(DESCRIPTION_SELECTOR)));
    return doc.select(DESCRIPTION_SELECTOR).text();
  }
}
