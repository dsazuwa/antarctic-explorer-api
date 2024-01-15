package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebElement;

public class HurtigrutenScraper extends Scraper {
  private static final String CRUISE_LINE_NAME = "Hurtigruten Expeditions";
  private static final String CRUISE_LINE_WEBSITE = "https://www.hurtigruten.com";
  private static final String EXPEDITION_WEBSITE =
      "https://www.hurtigruten.com/en-us/expeditions/cruises/?forceRefresh=true&destinations=antarctica-cruises";

  private static final String EXPEDITION_SELECTOR =
      "a.w-full.link-wrapper.group[data-testid=\"cruise-card-link\"]";
  private static final String NEXT_PAGE_BTN_SELECTOR = "nav#pagination > a.iconBtn";
  private static final String CURRENT_PAGE_SELECTOR = "nav#pagination > button[disabled]";

  private static final String PAGE_SUMMARY_SELECTOR =
      "div.my-4 > div > p.body-text-2.text-light-black";
  private static final String NAME_SELECTOR = "h3[data-testid=\"cruisecard-heading\"]";
  private static final String DURATION_SELECTOR = "div > div > div > div.ml-2";
  private static final String PHOTO_SELECTOR =
      "div > span > img[data-testid=\"cruisecard-header-image\"]";
  private static final String PRICE_SELECTOR =
      "div.flex.items-start > div.flex > div.flex > p[data-testid=\"feature-item-value\"]";
  private static final String DESCRIPTION_SELECTOR =
      "div.flex.flex-col.w-full > div > div.flex > p.mb-0";

  public HurtigrutenScraper(
      CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
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

      expeditions.forEach(this::processExpedition);
    } finally {
      quitDriver();
    }
  }

  @Override
  protected String getCurrentPageText() {
    return findElement(CURRENT_PAGE_SELECTOR).getText();
  }

  private boolean hasNextPage() {
    String[] parts = findElement(PAGE_SUMMARY_SELECTOR).getText().split("\\s+");

    int currentPageEnd = Integer.parseInt(parts[2]);
    int totalCruises = Integer.parseInt(parts[4]);

    return currentPageEnd < totalCruises;
  }

  private void navigateToNextPage() {
    String prevPage = getCurrentPageText();

    WebElement nextPage = findElements(NEXT_PAGE_BTN_SELECTOR).get(1);
    String href = nextPage.getAttribute("href");

    navigateTo(href, () -> waitForPageChange(prevPage));
  }

  private Elements scrapeExpeditions() {
    return getParsedPageSource().select(EXPEDITION_SELECTOR);
  }

  private void processExpedition(Element expedition) {
    String website = cruiseLine.getWebsite() + expedition.attr("href");
    String name = expedition.select(NAME_SELECTOR).text();
    String duration = expedition.select(DURATION_SELECTOR).text();
    BigDecimal startingPrice = extractPrice(expedition, PRICE_SELECTOR);
    String photoUrl = expedition.select(PHOTO_SELECTOR).attr("src");

    if (!navigateToExpeditionWebsite(website)) return;

    String description = extractDescription();

    expeditionService.saveIfNotExist(
        cruiseLine, website, name, description, null, null, duration, startingPrice, photoUrl);
  }

  private boolean navigateToExpeditionWebsite(String website) {
    try {
      navigateTo(website, DESCRIPTION_SELECTOR);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String extractDescription() {
    return findElements(DESCRIPTION_SELECTOR).stream()
        .map(e -> e.getText().trim())
        .collect(Collectors.joining(". "));
  }
}
