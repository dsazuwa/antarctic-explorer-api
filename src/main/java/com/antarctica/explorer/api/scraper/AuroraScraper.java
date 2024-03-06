package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.model.Itinerary;
import com.antarctica.explorer.api.model.Vessel;
import com.antarctica.explorer.api.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AuroraScraper extends Scraper {
  private static final String EXPEDITION_SELECTOR = "div.col-lg-4.py-3 > div.block-offer";
  private static final String CURRENT_PAGE_SELECTOR = "div.wp-pagenavi > span.current";
  private static final String NEXT_PAGE_SELECTOR = "div.wp-pagenavi > a.nextpostslink";
  private static final String PORT_SELECTOR = "div.inner-content > div.details > dl.clearfix > dd";

  public AuroraScraper(
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
        "Aurora Expeditions");
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

      expeditions.forEach(this::scrapeExpedition);
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

  private void scrapeExpedition(Element element) {
    Elements title = element.select("h4.mb-2 > a");
    String name = title.text();
    String website = title.attr("href");
    String duration = element.select("div.col > p.font-weight-bold").text().split(" ")[0];

    navigateTo(website);
    Document doc = getParsedPageSource();
    if (doc.select(PORT_SELECTOR).isEmpty()) return;

    Expedition expedition = processExpedition(doc, element, name, website, duration);
    scrapeGallery(doc, expedition);
    scrapeDeparture(doc, expedition);
  }

  private Expedition processExpedition(
      Document doc, Element element, String name, String website, String duration) {
    String photoSelector = "a > div.embed-responsive-item";
    String descriptionSelector = "div.container > div.row.section > div > p";
    String priceSelector = "div.col > p.price > span.price__value";

    BigDecimal startingPrice = extractPrice(element, priceSelector);
    String photoUrl = extractPhotoUrl(element, photoSelector, "style", "url('", "')");

    Elements descriptionElements = doc.select(descriptionSelector);
    String[] description =
        (descriptionElements.isEmpty()) ? null : extractDescription(descriptionElements);

    String[] highlights = extractHighlights(doc);
    String[] ports = extractPorts(doc);

    return expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        highlights,
        ports[0],
        ports[1],
        duration,
        startingPrice,
        photoUrl);
  }

  private void scrapeGallery(Document doc, Expedition expedition) {
    String gallerySelector = "div.gallery-wrapper > div";
    String imageSelector = "div.carousel-item > a";

    for (Element photo :
        Objects.requireNonNull(doc.selectFirst(gallerySelector)).select(imageSelector)) {
      String url = photo.attr("href");
      if (url.isEmpty()) continue;

      String alt = photo.select("p").text();
      expeditionService.saveGalleryImg(expedition, url, alt.isEmpty() ? null : alt);
    }
  }

  private void scrapeDeparture(Document doc, Expedition expedition) {
    String optionSelector = "div.details > dl.clearfix > dd > select > option";
    String startingPriceSelector = "dd > h6.old-price > span.price__value";
    String discountedPriceSelector = "dd > h4 > span.price__value";

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
    String mainWebsite = getCurrentUrl();

    for (Element option : doc.select(optionSelector)) {
      String[] dates = option.text().split(" - ");
      LocalDate startDate = LocalDate.parse(dates[0], formatter);
      LocalDate endDate = LocalDate.parse(dates[1], formatter);

      String name = option.attr("value");
      String website = mainWebsite + "?code=" + name;
      navigateTo(
          website,
          () ->
              wait.until(
                  ExpectedConditions.textToBePresentInElement(
                      findElements("div.details > dl.clearfix > dd").get(2), name)));

      Document departureDoc = getParsedPageSource();

      Vessel vessel = getVessel(departureDoc);
      if (vessel == null) {
        Optional<Vessel> randVessel = vesselService.findOneByCruiseLIne(cruiseLine);
        if (randVessel.isEmpty()) return;
        vessel = randVessel.get();
      }

      BigDecimal[] prices =
          extractPrices(departureDoc, startingPriceSelector, discountedPriceSelector);

      departureService.saveDeparture(
          expedition,
          vessel,
          getItinerary(departureDoc, expedition),
          name,
          startDate,
          endDate,
          prices[0],
          prices[1],
          website);
    }
  }

  private Vessel getVessel(Document doc) {
    String shipSelector = "div.details > dl.clearfix > dd";

    Element shipElement = doc.select(shipSelector).get(4).selectFirst("a");
    if (shipElement == null) return null;
    String name = shipElement.text();
    String website = shipElement.attr("href");

    Optional<Vessel> vessel = vesselService.findByName(name);
    return vessel.orElseGet(() -> scrapeVessel(website, name));
  }

  private Vessel scrapeVessel(String website, String name) {
    String descriptionSelector = "div.generic-content > p";
    String detailSelector = "div > p > span.prop";

    navigateTo(website, descriptionSelector);
    Document doc = getParsedPageSource();

    Elements descriptionElements = doc.select(descriptionSelector);
    String[] description =
        IntStream.range(0, descriptionElements.size())
            .filter(i -> i < 4)
            .mapToObj(i -> descriptionElements.get(i).text())
            .filter(text -> !text.isEmpty())
            .toArray((String[]::new));

    int capacity =
        Integer.parseInt(
            Objects.requireNonNull(doc.selectFirst(detailSelector)).text().replaceAll("\\D", ""));

    int cabins =
        Integer.parseInt(
            Objects.requireNonNull(doc.select(detailSelector).get(1)).text().replaceAll("\\D", ""));

    String photoUrl =
        name.equalsIgnoreCase("Greg Mortimer")
            ? "https://upload.wikimedia.org/wikipedia/commons/6/6f/Greg_Mortimer_IMO_9834648_P_Antarctica_03-01-2020.jpg"
            : "https://www.auroraexpeditions.com.au/wp-content/uploads/2020/01/Sylvia-Earle-in-Antarctica-scaled.jpg";

    return vesselService.saveIfNotExist(
        cruiseLine, name, description, capacity, cabins, website, photoUrl);
  }

  private Itinerary getItinerary(Document doc, Expedition expedition) {
    String mapSelector = "div.map-wrapper > a";
    String itinerarySelector = "div.section-itinerary > div.accordion > div > div";
    String headerSelector = "a.media > div.media-body > h4";
    String contentSelector = "div.collapse > div.generic-content > p";

    String[] ports = extractPorts(doc);
    String mapUrl = doc.select(mapSelector).attr("href");

    List<Itinerary> existingItinerary =
        itineraryService.getItinerary(expedition, ports[0], ports[1]);
    if (!existingItinerary.isEmpty()) return existingItinerary.get(0);

    String duration =
        Objects.requireNonNull(doc.select(PORT_SELECTOR).get(0).select("span").last()).text();
    Itinerary itinerary =
        itineraryService.saveItinerary(expedition, null, ports[0], ports[1], duration, mapUrl);

    for (Element element : doc.select(itinerarySelector)) {
      String[] headerParts = element.select(headerSelector).text().split(" ");
      String day = headerParts[0] + " " + headerParts[1];
      String header = String.join(" ", Arrays.copyOfRange(headerParts, 2, headerParts.length));

      String[] content =
          element.select(contentSelector).stream()
              .map(Element::text)
              .filter(text -> !text.isEmpty())
              .toArray(String[]::new);

      itineraryService.saveItineraryDetail(itinerary, day, header, content);
    }

    return itinerary;
  }

  /**
   * somewhat dirty workaround to address Aurora's habit of including a welcome message in
   * description. It also handles Aurora's interesting layout
   *
   * @param elements the elements to be processed
   * @return an array of string representing each paragraph of the description
   */
  private String[] extractDescription(Elements elements) {
    String[] description = elements.stream().map(Element::text).toArray(String[]::new);

    return Arrays.stream(
            (description.length > 1)
                ? Arrays.copyOfRange(description, 1, description.length)
                : description)
        .filter(x -> !x.contains("•") && !x.isEmpty())
        .toArray(String[]::new);
  }

  private String[] extractHighlights(Document doc) {
    String[] highlights =
        getHighlights(doc, "div.container > div.section > div.col-xl-8 > div.section > p");
    if (highlights.length != 0) return highlights;

    highlights = getHighlights(doc, "div.container.container-sm > div.section > div.section > p");
    if (highlights.length != 0) return highlights;

    return doc.select("div.section > ul > li > span").stream()
        .map(Element::text)
        .filter(x -> !x.isEmpty())
        .toArray(String[]::new);
  }

  private String[] getHighlights(Document doc, String selector) {
    return doc.select(selector).stream()
        .map(x -> x.text().replace("•", "").replace(".", "").trim())
        .filter(x -> !x.isEmpty())
        .toArray(String[]::new);
  }

  private String[] extractPorts(Document doc) {
    String[] ports = doc.select(PORT_SELECTOR).get(1).text().split(" - ");

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }
}
