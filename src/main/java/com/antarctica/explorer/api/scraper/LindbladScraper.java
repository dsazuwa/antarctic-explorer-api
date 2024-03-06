package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.model.Itinerary;
import com.antarctica.explorer.api.model.Vessel;
import com.antarctica.explorer.api.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LindbladScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = "div.card_cardContainer__vyvNi";
  private static final String DESCRIPTION_SELECTOR =
      "div.sc-c71aec9f-2.dVGsho > p.sc-1a030b44-1.ka-dLeA";
  private static final String PORT_SELECTOR =
      "header.sc-36842228-9.dFwmLF > h3.sc-36842228-12.jKNlCi > div.sc-12a2b3de-0.kCEMBM > div.sc-12a2b3de-1.fnHsb > span.sc-12a2b3de-3.cvVhAe";
  private static final String ITINERARY_SELECTOR =
      "div.sc-36842228-3.hBGyOc > div.sc-ad096f17-1.ebIpYW";
  private static final String SHIP_SELECTOR = "div[data-module=ship]";

  private boolean newsletterRemoved;

  public LindbladScraper(
      CruiseLineService cruiseLineService,
      VesselService vesselService,
      ExpeditionService expeditionService,
      ItineraryService itineraryService,
      DepartureService departureService,
      ExtensionService extensionService) {
    super(
        cruiseLineService,
        vesselService,
        expeditionService,
        itineraryService,
        departureService,
        extensionService,
        "Lindblad Expeditions");
  }

  @Override
  public void scrape() {
    try {
      newsletterRemoved = false;
      scrapeExpeditions();
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
    String newsletterSelector = "div.sc-67e7c0e-1.cqMfmY > button.sc-ec262d12-0.bMrTos";

    waitForPresenceOfElement(newsletterSelector);
    WebElement closeButton = findElement(newsletterSelector);
    if (closeButton != null) {
      closeButton.click();
      newsletterRemoved = true;
    }
  }

  private void scrapeExpeditions() {
    navigateTo(cruiseLine.getExpeditionWebsite(), EXPEDITION_SELECTOR);
    acceptCookie();
    loadLazyImage();
    getParsedPageSource().select(EXPEDITION_SELECTOR).forEach(this::processExpedition);
  }

  private void loadLazyImage() {
    for (WebElement listItem : findElements(EXPEDITION_SELECTOR)) {
      getExecutor().executeScript("arguments[0].scrollIntoView(true);", listItem);

      WebElement image =
          listItem.findElement(By.cssSelector("div.card_cardContainer__vyvNi > div > span"));
      wait.until(ExpectedConditions.attributeContains(image, "class", "lazy-load-image-loaded"));
    }
  }

  private void processExpedition(Element element) {
    Expedition expedition = scrapeExpedition(element);
    scrapeItineraries(expedition);

    Document doc = getDocument();
    scrapeGallery(doc, expedition);
    scrapeVessels(doc);
    scrapeDepartures(doc, expedition);
  }

  private Expedition scrapeExpedition(Element element) {
    String nameSelector = "a.card_name__GotR3";
    String duration_selector = "span.card_days__B1niQ";
    String priceSelector = "div.card_price___eMSv > span.card_amount__VxXVs";
    String imageSelector = "img.card_image___G1Nm";
    String highlightSelector = "ul.sc-d5179a42-0.jKPGuL > li > p";
    String departureSelector = "ol[data-module=departureCardList] > li";

    Element nameElement = Objects.requireNonNull(element.selectFirst(nameSelector));
    String name = nameElement.text();
    String website = cruiseLine.getWebsite() + nameElement.attr("href");

    String duration = element.select(duration_selector).text().split(" ")[0];
    BigDecimal price = extractPrice(element, priceSelector);
    String photoUrl = Objects.requireNonNull(element.selectFirst(imageSelector)).attr("src");

    navigateTo(
        website,
        new String[] {
          DESCRIPTION_SELECTOR, SHIP_SELECTOR, PORT_SELECTOR, ITINERARY_SELECTOR, departureSelector
        });

    if (!newsletterRemoved) removeNewsletter();

    String[] description =
        findElements(DESCRIPTION_SELECTOR).stream().map(WebElement::getText).toArray(String[]::new);
    String[] ports =
        findElements(PORT_SELECTOR).stream().map(WebElement::getText).toArray(String[]::new);
    String[] highlights =
        findElements(highlightSelector).stream().map(WebElement::getText).toArray(String[]::new);

    return expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        highlights,
        ports[0],
        ports[1],
        duration,
        price,
        photoUrl);
  }

  private void scrapeItineraries(Expedition expedition) {
    String buttonSelector = "div.sc-cde596b-1.fJKQlx > button.sc-cde596b-4.jTBrAO";
    String boundarySelector = "#day-by-day > h2";
    String buttonNameSelector = "span.sc-cde596b-2.hRkFEa";
    String itinerarySelector = "div.sc-36842228-0.Anwop.sc-d6abfba5-0.euRrRz";
    String itineraryNameSelector =
        "div.sc-36842228-11.ceeEMz > h3.sc-91ccd5f9-0.sc-36842228-14.EjOQT.VGHIR";

    List<WebElement> buttons = findElements(buttonSelector);

    if (buttons.isEmpty()) scrapeItinerary(findElement(itinerarySelector), expedition);
    else {
      waitForPresenceOfElement(boundarySelector);
      WebElement boundary = findElement(boundarySelector);

      for (WebElement button : buttons) {
        try {
          scrollIntoViewAndClick(button, boundary);
        } catch (ElementClickInterceptedException e) {
          scrollIntoViewAndClick(button, boundary);
        }

        wait.until(
            (WebDriver wd) -> {
              String buttonName = findElement(button, buttonNameSelector).getText();
              String itineraryName = findElement(itineraryNameSelector).getText();

              return buttonName.equalsIgnoreCase(itineraryName);
            });

        scrapeItinerary(findElement(itinerarySelector), expedition);
      }
    }
  }

  private void scrollIntoViewAndClick(WebElement button, WebElement boundary) {
    getExecutor().executeScript("arguments[0].scrollIntoView(true);", boundary);
    wait.until(ExpectedConditions.visibilityOf(boundary));
    button.click();
  }

  private void scrapeItinerary(WebElement webElement, Expedition expedition) {
    String nameSelector = "h3.sc-91ccd5f9-0.sc-36842228-14.EjOQT.VGHIR";
    String detailSelector = "div.sc-ad096f17-1.ebIpYW";
    String daySelector = "p.sc-dd73f2f5-0.sc-ad096f17-6.bMGjHV.bvzlzR";

    Element element = Jsoup.parse(webElement.getAttribute("innerHTML"));
    String name = element.select(nameSelector).text();

    String[] ports =
        element.select(PORT_SELECTOR).stream().map(Element::text).toArray(String[]::new);

    Elements details = element.select(detailSelector);
    String duration =
        details.get(details.size() - 1).select(daySelector).text().replaceAll("[^0-9]", "");

    Itinerary itinerary =
        itineraryService.saveItinerary(
            expedition,
            name.equalsIgnoreCase("Expedition") ? null : name,
            ports[0],
            ports[1],
            duration,
            null);

    for (Element detail : details) scrapeItineraryDetail(detail, itinerary);
  }

  private void scrapeItineraryDetail(Element element, Itinerary itinerary) {
    String daySelector = "p.sc-dd73f2f5-0.sc-ad096f17-6.bMGjHV.bvzlzR";
    String headerSelector = "h4.sc-91ccd5f9-0.sc-ad096f17-5.gZhyTX.eSHMXQ";
    String contentSelector = "p.sc-dd73f2f5-0.sc-1a030b44-0.gQUCHt.dzbrZi";

    String day = element.select(daySelector).text();
    String header = Objects.requireNonNull(element.select(headerSelector).first()).ownText();
    String[] content =
        element.select(contentSelector).stream().map(Element::text).toArray(String[]::new);

    itineraryService.saveItineraryDetail(itinerary, day, header, content);
  }

  private void scrapeGallery(Document doc, Expedition expedition) {
    String photoSelector = "div.sc-404189a-6.hsqTTv > img";

    for (Element photo : doc.select(photoSelector)) scrapeGalleryImg(expedition, photo);
  }

  private void scrapeGalleryImg(Expedition expedition, Element photo) {
    String url = photo.attr("src").split("\\?io")[0];
    String alt = photo.attr("alt");
    expeditionService.saveGalleryImg(expedition, url, alt);
  }

  private void scrapeVessels(Document doc) {
    String nameSelector = "h3";
    String descriptionSelector = "div > p.sc-1a030b44-1.ka-dLeA";
    String detailSelector = "div.sc-90fd9a9-5.cdDXJF > p.sc-dd73f2f5-0.sc-90fd9a9-7.gQUCHt.hMywje";
    String linkSelector = "div.sc-90fd9a9-10.bgbWLg > a.sc-baf605bd-2.gXAMNb";
    String pictureSelector = "div.sc-90fd9a9-11.fmQQTH > img";

    doc.select(SHIP_SELECTOR)
        .forEach(
            element -> {
              String name = element.select(nameSelector).text();
              String[] description =
                  element.select(descriptionSelector).stream()
                      .map(Element::text)
                      .filter(text -> !text.isEmpty())
                      .toArray(String[]::new);

              int capacity =
                  Integer.parseInt(
                      Objects.requireNonNull(element.select(detailSelector).get(0)).text());

              int cabins =
                  Integer.parseInt(
                      Objects.requireNonNull(element.select(detailSelector).get(1)).text());

              String website = cruiseLine.getWebsite() + element.select(linkSelector).attr("href");
              String photoUrl = element.select(pictureSelector).attr("src");

              vesselService.saveIfNotExist(
                  cruiseLine, name, description, capacity, cabins, website, photoUrl);
            });
  }

  private void scrapeDepartures(Document doc, Expedition expedition) {
    String selector = "ol.sc-92502399-0.DfNNS > li";

    int year = 0;
    for (Element element : doc.select(selector)) {
      Element yearElement = element.selectFirst("h3");

      if (yearElement != null) year = Integer.parseInt(yearElement.text());
      else scrapeDeparture(expedition, element, year);
    }
  }

  private void scrapeDeparture(Expedition expedition, Element element, int year) {
    String startingPriceSelector = "p > span > span.sc-596b3848-5.iSVNMC";
    String priceSelector = "p > span.sc-596b3848-2.KSERt";
    String dateSelector = "div > p.sc-ab43b34d-9.eFOFNH";
    String linkSelector = "a";
    String vesselSelector = "div.sc-ab43b34d-8.eVwwkY > i";
    String nameSelector = "p.sc-ab43b34d-6.kmWQpO";

    BigDecimal[] prices = extractPrices(element, startingPriceSelector, priceSelector);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    LocalDate[] dates =
        element.select(dateSelector).stream()
            .map(Element::text)
            .map(x -> LocalDate.parse(x + ", " + year, formatter))
            .toArray(LocalDate[]::new);

    String website = cruiseLine.getWebsite() + element.select(linkSelector).attr("href");

    String vesselName = element.select(vesselSelector).text();
    Vessel vessel = vesselService.getByName(vesselName);

    String itineraryName = Objects.requireNonNull(element.select(nameSelector).first()).ownText();
    List<Itinerary> itinerary =
        itineraryName.equalsIgnoreCase("Expedition")
            ? itineraryService.getItinerary(expedition)
            : itineraryService.getItinerary(expedition, itineraryName);
    if (itinerary.isEmpty()) return;

    departureService.saveDeparture(
        expedition,
        vessel,
        itinerary.get(0),
        null,
        dates[0],
        dates[1],
        prices[0],
        prices[1],
        website);
  }

  private Document getDocument() {
    String buttonSelector = "ol[data-module=departureCardList] > button.sc-baf605bd-1.cwYkyy",
        lastChildXPathExpression = "(//ol[@data-module='departureCardList']/li)[last()]",
        bottomSelector = "section[aria-label='Newsletter sign up']";

    while (true) {
      WebElement button;

      try {
        button = findElement(buttonSelector);
      } catch (NoSuchElementException e) {
        break;
      }

      JavascriptExecutor executor = getExecutor();
      executor.executeScript(
          "arguments[0].scrollIntoView(true);", findElement(By.xpath(lastChildXPathExpression)));
      wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(bottomSelector)));

      try {
        executor.executeScript("arguments[0].click();", button);
      } catch (StaleElementReferenceException e) {
        break;
      }
    }

    return getParsedPageSource();
  }
}
