package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LindbladScraper extends Scraper {
  private static final String CRUISE_LINE_NAME = "Lindblad Expeditions";
  private static final String CRUISE_LINE_WEBSITE = "https://world.expeditions.com";
  private static final String EXPEDITION_WEBSITE =
      "https://world.expeditions.com/book?destinations.name=Antarctica";
  private static final String LOGO_URL =
      "https://media.licdn.com/dms/image/C560BAQFXWQLkE4zOjQ/company-logo_200_200/0/1630601424707/lindblad_expeditions_logo?e=1714608000&v=beta&t=Fu0aR7UXH96TN0BAn9qzQsX3pDSp1u6I5Aa0sWmkq68";

  private static final String EXPEDITION_SELECTOR = "div.card_cardContainer__vyvNi";
  private static final String DESCRIPTION_SELECTOR =
      "div.sc-c71aec9f-2.dVGsho > p.sc-1a030b44-1.ka-dLeA";
  private static final String PORT_SELECTOR =
      "div.sc-36842228-0.Anwop.sc-d6abfba5-0.euRrRz > header.sc-36842228-9.dFwmLF > h3.sc-36842228-12.jKNlCi > div.sc-12a2b3de-0.kCEMBM > div.sc-12a2b3de-1.fnHsb > span.sc-12a2b3de-3.cvVhAe";
  private static final String HIGHLIGHT_SELECTOR = "ul.sc-d5179a42-0.jKPGuL > li > p";
  private static final String ITINERARY_SELECTOR =
      "div.sc-36842228-3.hBGyOc > div.sc-ad096f17-1.ebIpYW";
  private static final String DEPARTURE_SELECTOR = "ol[data-module=departureCardList] > li";

  private boolean newsletterRemoved;

  public LindbladScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
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
      newsletterRemoved = false;

      navigateTo(cruiseLine.getExpeditionWebsite());
      acceptCookie();

      getParsedPageSource().select(EXPEDITION_SELECTOR).forEach(this::processExpedition);
    } finally {
      quitDriver();
    }
  }

  @Override
  protected String getCurrentPageText() {
    return "1";
  }

  private void acceptCookie() {
    String cookieSelector = "button.sc-baf605bd-1.flHA-dN";

    waitForPresenceOfElement(cookieSelector);
    WebElement acceptCookieButton = findElement(cookieSelector);
    acceptCookieButton.click();
  }

  private void removeNewsletter() {
    String newsletterSelector = "div.sc-7f64236d-1.iUClex > button.sc-ec262d12-0.bMrTos";

    waitForPresenceOfElement(newsletterSelector);
    WebElement closeButton = findElement(newsletterSelector);
    if (closeButton != null) {
      closeButton.click();
      newsletterRemoved = true;
    }
  }

  private void processExpedition(Element element) {
    Expedition expedition = scrapeExpedition(element);

    Document doc = getParsedPageSource();
    scrapeItineraries(doc, expedition);
    scrapeDepartures(doc, expedition);
  }

  private Expedition scrapeExpedition(Element element) {
    String nameSelector = "a.card_name__GotR3";
    String duration_selector = "span.card_days__B1niQ";
    String priceSelector = "div.card_price___eMSv > span.card_amount__VxXVs";
    String imageSelector = "div.sc-404189a-6.hsqTTv > img.sc-a2b32e3-4.kidUre";

    Element nameElement = Objects.requireNonNull(element.selectFirst(nameSelector));
    String name = nameElement.text();
    String website = cruiseLine.getWebsite() + nameElement.attr("href");

    String duration = element.select(duration_selector).text().split(" ")[0];
    BigDecimal price = extractPrice(element, priceSelector);

    navigateTo(
        website,
        new String[] {DESCRIPTION_SELECTOR, PORT_SELECTOR, ITINERARY_SELECTOR, DEPARTURE_SELECTOR});

    if (!newsletterRemoved) removeNewsletter();

    Document doc = getDocument();
    String photoUrl = Objects.requireNonNull(doc.selectFirst(imageSelector)).attr("src");
    String description = doc.select(DESCRIPTION_SELECTOR).text();
    String[] ports = extractPorts(doc);
    String[] highlights =
        doc.select(HIGHLIGHT_SELECTOR).stream().map(Element::text).toArray(String[]::new);

    return expeditionService.saveIfNotExist(
        cruiseLine, website, name, description, ports[0], ports[1], duration, price, photoUrl);
  }

  private Document getDocument() {
    String buttonSelector = "ol[data-module=departureCardList] > button.sc-baf605bd-1.cwYkyy";
    String lastDepartureSelector = "(//ol[@data-module='departureCardList']/li)[last()]";
    String newsLetterSelector = "section[aria-label='Newsletter sign up']";

    while (true) {
      WebElement button;

      try {
        button = findElement(buttonSelector);
      } catch (NoSuchElementException e) {
        break;
      }

      JavascriptExecutor executor = getExecutor();
      executor.executeScript(
          "arguments[0].scrollIntoView(true);", findElement(By.xpath(lastDepartureSelector)));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(newsLetterSelector)));

      try {
        executor.executeScript("arguments[0].click();", button);
      } catch (StaleElementReferenceException e) {
        break;
      }
    }

    return getParsedPageSource();
  }

  private void scrapeItineraries(Document doc, Expedition expedition) {
    doc.select(ITINERARY_SELECTOR)
        .forEach(
            element -> {
              String daySelector = "p.sc-dd73f2f5-0.sc-ad096f17-6.bMGjHV.bvzlzR";
              String headerSelector = "h4.sc-91ccd5f9-0.sc-ad096f17-5.gZhyTX.eSHMXQ:not(:has(p))";
              String contentSelector = "p.sc-dd73f2f5-0.sc-1a030b44-0.gQUCHt.dzbrZi";

              String day = element.select(daySelector).text();
              String header = element.select(headerSelector).text();
              String content =
                  element.select(contentSelector).stream()
                      .map(Element::text)
                      .collect(
                          StringBuilder::new,
                          (sb, text) -> sb.append(text).append("\n"),
                          StringBuilder::append)
                      .toString();

              expeditionService.saveItinerary(expedition, day, header, content);
            });
  }

  private void scrapeDepartures(Document doc, Expedition expedition) {
    String cardSelector = "ol.sc-487915d1-0.mwJrq > li";
    String nameSelector = "p.sc-ba3f9bc9-5.eAYUGq";
    String priceSelector = "div > div.sc-ba3f9bc9-2.jxCzLd > div > p > span.sc-d219990a-2.dxQAZd";
    String portSelector = "p.sc-ba3f9bc9-5.eAYUGq > span.sc-ba3f9bc9-4.deMCur";
    String dateSelector = "div > div.sc-ba3f9bc9-1.jVDvlO > div > div > p.sc-ba3f9bc9-10.ILPgp";

    int year = 0;
    for (Element card : doc.select(cardSelector)) {
      Element yearElement = card.selectFirst("h3");

      if (yearElement != null) year = Integer.parseInt(yearElement.text());
      else {
        String name = Objects.requireNonNull(card.select(nameSelector).first()).text();
        BigDecimal price = extractPrice(card, priceSelector);
        String[] ports = card.select(portSelector).text().split(" → ");

        int finalYear = year;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
        LocalDate[] dates =
            card.select(dateSelector).stream()
                .map(Element::text)
                .map(x -> LocalDate.parse(x + ", " + finalYear, formatter))
                .toArray(LocalDate[]::new);

        expeditionService.saveDeparture(
            expedition,
            name.equalsIgnoreCase("Expedition") ? null : name,
            ports.length != 2 ? null : ports[0],
            ports.length != 2 ? null : ports[1],
            dates[0],
            dates[1],
            price,
            null);
      }
    }
  }

  private String[] extractPorts(Document doc) {
    String[] ports = doc.select(PORT_SELECTOR).stream().map(Element::text).toArray(String[]::new);

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }
}
