package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VikingScraper extends Scraper {
  private static final String CRUISE_LINE_NAME = "Viking Expeditions";
  private static final String CRUISE_LINE_WEBSITE = "https://www.vikingcruises.com";
  private static final String EXPEDITION_WEBSITE =
      "https://www.vikingcruises.com/expeditions/search-cruises/index.html?Regions=Antarctica";
  private static final String LOGO_URL = "https://centraltravel.com/images/Viking-logo.jpg";

  private static final String EXPEDITION_SELECTOR = "div.cruise-detail-wrapper > div.cruise-detail";
  private static final String DESCRIPTION_SELECTOR = ".hero-sidebar-content > div.description";

  private static final String PHOTO_SELECTOR = "img.cruise-link.thumbnail-img";
  private static final String NAME_SELECTOR = "div.cruise-title-wrapper > a.cruise-link > h3";
  private static final String WEBSITE_SELECTOR = "div.cruise-title-wrapper > a.cruise-link";
  private static final String PORT_SELECTOR = "div.cruise-title-wrapper > h4";
  private static final String ITEM_VALUE_SELECTOR =
      "section.info > div.detail > div > div.item > span.value";

  public VikingScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
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
      Document doc = getParsedPageSource();
      Elements expeditions = doc.select(EXPEDITION_SELECTOR);

      expeditions.forEach(this::processExpedition);
    } finally {
      quitDriver();
    }
  }

  @Override
  protected String getCurrentPageText() {
    return "1";
  }

  private void processExpedition(Element item) {
    String photoUrl = item.select(PHOTO_SELECTOR).attr("src");
    String name = item.select(NAME_SELECTOR).text();
    String website = cruiseLine.getWebsite() + item.select(WEBSITE_SELECTOR).attr("href");

    String[] ports = item.select(PORT_SELECTOR).text().split(" to ");
    String startingPort = ports.length > 0 ? ports[0] : null;
    String endingPort = ports.length > 1 ? ports[1] : null;

    Elements infoElements = item.select(ITEM_VALUE_SELECTOR);
    String duration = infoElements.get(0).text();
    BigDecimal startingPrice = extractPrice(infoElements.get(infoElements.size() - 1).text());

    navigateTo(website, DESCRIPTION_SELECTOR);
    Document doc = getParsedPageSource();

    String description = doc.select(DESCRIPTION_SELECTOR).text();

    expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        startingPort,
        endingPort,
        duration,
        startingPrice,
        photoUrl);
  }
}
