package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;

public class AuroraScraper extends Scraper {
  private static final String CRUISE_LINE_NAME = "Aurora Expeditions";
  private static final String CRUISE_LINE_WEBSITE =
      "https://www.aurora-expeditions.com/destination";
  private static final String EXPEDITION_WEBSITE =
      "https://www.aurora-expeditions.com/find-an-expedition/?destinations%5B%5D=antarctica-cruises&destinations%5B%5D=antarctic-peninsula&destinations%5B%5D=weddell-sea&destinations%5B%5D=south-georgia-island&destinations%5B%5D=falkland-islands-malvinas&destinations%5B%5D=antarctic-circle";
  private static final String LOGO_URL =
      "https://media.glassdoor.com/sql/2542964/aurora-expeditions-squarelogo-1643802057576.png";

  private static final String EXPEDITION_SELECTOR = "div.col-lg-4.py-3 > div.block-offer";
  private static final String CURRENT_PAGE_SELECTOR = "div.wp-pagenavi > span.current";
  private static final String NEXT_PAGE_SELECTOR = "div.wp-pagenavi > a.nextpostslink";

  private static final String TITLE_SELECTOR = "h4.mb-2 > a";
  private static final String PRICE_SELECTOR = "div.col > p.price > span.price__value";
  private static final String DESCRIPTION_SELECTOR = "div.container > div.row.section > div > p";
  private static final String PORTS_SELECTOR = "div.inner-content > div.details > dl.clearfix > dd";
  private static final String DURATION_SELECTOR = "div.col > p.font-weight-bold";
  private static final String PHOTO_SELECTOR = "a > div.embed-responsive-item";

  public AuroraScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(
        cruiseLineService,
        expeditionService,
        CRUISE_LINE_NAME,
        CRUISE_LINE_WEBSITE,
        EXPEDITION_WEBSITE,
        LOGO_URL);
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
    return !findElements(NEXT_PAGE_SELECTOR).isEmpty();
  }

  private void navigateToNextPage() {
    String prevPage = getCurrentPageText();
    String href = findElement(NEXT_PAGE_SELECTOR).getAttribute("href");
    navigateTo(href, () -> waitForPageChange(prevPage));
  }

  private Elements scrapeExpeditions() {
    return getParsedPageSource().select(EXPEDITION_SELECTOR);
  }

  private void processExpedition(Element item) {
    Elements title = item.select(TITLE_SELECTOR);
    String name = title.text();
    String website = title.attr("href");

    String duration = item.select(DURATION_SELECTOR).text().replaceAll("[A-Za-z\\s]", "");
    BigDecimal startingPrice = extractPrice(item, PRICE_SELECTOR);
    String photoUrl = extractPhotoUrl(item, PHOTO_SELECTOR, "style", "url('", "')");

    navigateTo(website, "div > h1");
    Document doc = getParsedPageSource();

    Elements descriptionElements = doc.select(DESCRIPTION_SELECTOR);
    if (descriptionElements.isEmpty()) return;

    String description = descriptionElements.text();
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

  private String[] extractPorts(Document doc) {
    String[] ports = doc.select(PORTS_SELECTOR).get(1).text().split(" - ");

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }
}
