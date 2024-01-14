package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebElement;

public class QuarkScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = ".views-row";
  private static final String AJAX_PROGRESS_SELECTOR = "div.ajax-progress-fullscreen";
  private static final String CURRENT_PAGE_SELECTOR =
      "ul.pagination > li.page-item.is-active.active";
  private static final String NEXT_PAGE_SELECTOR =
      "ul.pagination > li.page-item.pager__item--next > a.page-link";

  private static final String WEBSITE_SELECTOR = "div.expedition__info > div > a";
  private static final String NAME_SELECTOR = "div.expedition__info > h2";
  private static final String DESCRIPTION_SELECTOR = "div.expedition__info > .expedition__body";
  private static final String PORT_SELECTOR = "div.detail--departing-from > ul > li";
  private static final String DURATION_SELECTOR = "div.detail--duration > p.detail__value";
  private static final String PRICE_SELECTOR = "span.detail__price";
  private static final String PHOTO_SELECTOR = "div.hero-banner__image > picture > img";

  public QuarkScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(cruiseLineService, expeditionService, "Quark Expeditions");
  }

  @Override
  public void scrape() {
    try {
      navigateTo(cruiseLine.getExpeditionWebsite(), EXPEDITION_SELECTOR);
      scrapeData();

      while (hasNextPage()) {
        navigateToNextPage();
        scrapeData();
      }
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
    WebElement element = findElement(NEXT_PAGE_SELECTOR);
    String website = element.getAttribute("href");
    navigateTo(website, () -> waitForInvisibilityOfElement(AJAX_PROGRESS_SELECTOR));
  }

  private void scrapeData() {
    Document doc = getParsedPageSource();
    Elements expeditionItems = doc.select(EXPEDITION_SELECTOR);

    for (Element item : expeditionItems) {
      String website = cruiseLine.getWebsite() + item.select(WEBSITE_SELECTOR).attr("href");
      String name = item.select(NAME_SELECTOR).text();
      String description = item.select(DESCRIPTION_SELECTOR).text();
      String port = item.select(PORT_SELECTOR).text();
      String duration = item.select(DURATION_SELECTOR).get(0).text();
      BigDecimal price = extractPrice(item, PRICE_SELECTOR);
      String photoUrl = cruiseLine.getWebsite() + item.select(PHOTO_SELECTOR).attr("src");

      expeditionService.saveIfNotExist(
          cruiseLine, website, name, description, port, port, duration, price, photoUrl);
    }
  }
}
