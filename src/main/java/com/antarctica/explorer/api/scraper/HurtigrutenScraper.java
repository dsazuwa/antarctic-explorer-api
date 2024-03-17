package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.*;
import com.antarctica.explorer.api.service.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class HurtigrutenScraper extends Scraper {

  private static final String EXPEDITION_SELECTOR =
      "a.w-full.link-wrapper.group[data-testid=\"cruise-card-link\"]";
  private static final String NEXT_PAGE_BTN_SELECTOR = "nav#pagination > a.iconBtn";
  private static final String CURRENT_PAGE_SELECTOR = "nav#pagination > button[disabled]";

  private static final String DESCRIPTION_SELECTOR =
      "div.flex.flex-col.w-full > div.w-full.max-w-full.contentfulTextBlock > p";
  private static final String SHIP_SELECTOR = "div[data-testid=\"explore-ship-container\"]";

  public HurtigrutenScraper(
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
        new CruiseLine(
            "Hurtigruten Expeditions",
            "https://www.hurtigruten.com",
            "https://www.hurtigruten.com/en-us/expeditions/ships/",
            "https://www.hurtigruten.com/en-us/expeditions/cruises/?forceRefresh=true&destinations=antarctica-cruises",
            "https://res.cloudinary.com/dcdakh7gh/image/upload/v1710007537/antarctica-explorer/HurtigrutenLogo.png"));
  }

  @Override
  public void scrape() {
    try {
      cruiseLineService.deleteExpeditionsAndExtensions(cruiseLine);

      scrapeVessels();

      navigateTo(cruiseLine.getExpeditionWebsite(), EXPEDITION_SELECTOR);
      Elements elements = scrapeExpeditions();

      while (hasNextPage()) {
        navigateToNextPage();
        elements.addAll(scrapeExpeditions());
      }

      for (Element element : elements) {
        String website = cruiseLine.getWebsite() + element.attr("href");
        if (!navigateToExpeditionWebsite(website)) continue;

        Expedition expedition = processExpedition(element);
        scrapeGallery(expedition);

        Itinerary itinerary = saveItinerary(expedition);
        saveItineraryDetails(itinerary);
        scrapeDeparture(expedition, itinerary);

        scrapeExtensions(expedition);
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
    String pageSummarySelector = "div.my-4 > div > p.body-text-2.text-light-black";
    String[] parts = findElement(pageSummarySelector).getText().split("\\s+");

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

  private void scrapeVessels() {
    String capacitySelector =
        "div[data-testid=\"tags-container\"] > div[data-testid=\"tag-non-clickable\"]";
    String highlightSelector = "div.grid.grid-cols-1.gap-3.my-6 > div.p-6.bg-white";
    String descriptionSelector = "p";

    Map<String, Integer> shipCabins =
        new HashMap<>() {
          {
            put("MS Fram", 125);
            put("MS Fridtjof Nansen", 265);
            put("MS Maud", 298);
            put("MS Roald Amundsen", 256);
            put("MS Santa Cruz II", 50);
            put("MS Spitsbergen", 102);
          }
        };

    navigateTo(cruiseLine.getFleetWebsite());
    lazyLoadImages(SHIP_SELECTOR, "img");

    for (Element element : getParsedPageSource().select(SHIP_SELECTOR)) {
      String name = element.select("h3").text();
      Optional<Vessel> vessel = vesselService.findByName(name);
      if (vessel.isPresent()) continue;

      String website = cruiseLine.getWebsite() + element.select("a").attr("href");
      int capacity =
          Integer.parseInt(element.select(capacitySelector).get(1).text().replaceAll("\\D", ""));
      String photoUrl = getImageUrl(element.select("img").attr("src"));

      navigateTo(website, highlightSelector);

      WebElement highlights = findElements(highlightSelector).get(0);
      List<WebElement> descElements = findElements(highlights, descriptionSelector);

      String[] description =
          descElements.stream()
              .map(WebElement::getText)
              .filter(text -> !text.isEmpty())
              .toArray((String[]::new));

      vesselService.save(
          cruiseLine,
          name,
          description,
          capacity,
          shipCabins.getOrDefault(name, null),
          website,
          photoUrl);
    }
  }

  private Elements scrapeExpeditions() {
    lazyLoadImages(EXPEDITION_SELECTOR, "span > img");
    return getParsedPageSource().select(EXPEDITION_SELECTOR);
  }

  private boolean navigateToExpeditionWebsite(String website) {
    try {
      navigateTo(website);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private Expedition processExpedition(Element element) {
    String nameSelector = "h3[data-testid=\"cruisecard-heading\"]";
    String durationSelector = "div > div > div > div.ml-2";
    String priceSelector =
        "div.flex.items-start > div.flex > div.flex > p[data-testid=\"feature-item-value\"]";
    String highlightSelector = "div.flex.mb-6 > p.mb-0";

    String website = cruiseLine.getWebsite() + element.attr("href");
    String name = element.select(nameSelector).text();
    String duration = element.select(durationSelector).text().replaceAll("[A-Za-z\\s]", "");
    BigDecimal startingPrice = extractPrice(element, priceSelector);
    String photoUrl = getImageUrl(element.select("img[alt=\"" + name + "\"]").attr("src"));

    String[] description =
        findElements(DESCRIPTION_SELECTOR).stream()
            .map(x -> x.getAttribute("textContent").trim())
            .toArray(String[]::new);

    String[] highlights =
        findElements(highlightSelector).stream()
            .map(x -> x.getAttribute("textContent").trim())
            .toArray(String[]::new);

    return expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        name,
        description,
        highlights,
        null,
        null,
        duration,
        startingPrice,
        photoUrl);
  }

  private void scrapeGallery(Expedition expedition) {
    String imageSelector = "ul > li > div > div > span > img";
    String buttonSelector =
        "div.items-center.justify-between.hidden.h-full.px-6 > button.aurora-button";

    JavascriptExecutor executor = getExecutor();
    WebElement nextButton = findElements(buttonSelector).get(1);

    for (int i = 0; i < findElements(imageSelector).size(); i++) {
      WebElement element = findElements(imageSelector).get(i);
      lazyLoadImage(element);

      String src = element.getAttribute("src");

      executor.executeScript("arguments[0].click();", nextButton);

      if (src.equalsIgnoreCase(
          "https://www.hurtigruten.com/img/placeholder.png?q=75&w=3840&fm=webp")) continue;
      expeditionService.saveGalleryImg(expedition, getImageUrl(src), null);
    }
  }

  private Itinerary saveItinerary(Expedition expedition) {
    String mapSelector =
        "div.react-transform-component.transform-component-module_content__FBWxo  > span > img";

    WebElement map = findElement(mapSelector);
    lazyLoadImage(map);

    return itineraryService.saveItinerary(
        expedition, null, null, null, expedition.getDuration(), map.getAttribute("src"));
  }

  private void saveItineraryDetails(Itinerary itinerary) {
    String detailSelector = "div.flex.flex-col.w-full.space-y-2 > div.flex.flex-row.flex-1";
    String daySelector = "h4.caption";
    String headerSelector =
        "h3.aurora-heading.aurora-heading-appearance-headline-3.aurora-heading-appearance-left";
    String contentSelector = "div.hidden > div > p";

    for (WebElement element : findElements(detailSelector))
      itineraryService.saveItineraryDetail(
          itinerary,
          findElement(element, daySelector).getAttribute("textContent"),
          findElement(element, headerSelector).getAttribute("textContent"),
          findElements(element, contentSelector).stream()
              .map(x -> x.getAttribute("textContent"))
              .toArray(String[]::new));
  }

  private void scrapeDeparture(Expedition expedition, Itinerary itinerary) {
    String sectionSelector =
        "div.aurora-layout.aurora-layout-grid-33_67.aurora-layout-border-none > div.aurora-column";
    String nextSelector = "button.btn.relative.btn-flat.white";
    String monthSelector = "button.flex.gap-2.bg-white.p-3.mb-3";
    String departureSelector = "div[data-testid='selectedVoyage']";
    String yearSelector = "div.flex.gap-2.p-3.mb-3.bg-white > span";

    waitForInvisibilityOfElement("div.spinner");

    JavascriptExecutor executor = getExecutor();
    WebElement section = findElements(sectionSelector).get(1);
    WebElement nextButton = findElements(section, nextSelector).get(1);

    boolean hasNextYear = true;
    while (hasNextYear) {
      WebElement yearElement = findElement(yearSelector);
      String year = yearElement.getText();

      List<WebElement> monthButtons =
          findElements(section, monthSelector).stream().filter(WebElement::isEnabled).toList();

      if (!year.isEmpty())
        for (WebElement monthButton : monthButtons) {
          getExecutor()
              .executeScript(
                  "arguments[0].scrollIntoView(true);", findElement("div[name='departures']"));
          executor.executeScript("arguments[0].click();", monthButton);

          List<WebElement> departures = findElements(section, departureSelector);
          departures.forEach(departure -> saveDeparture(departure, year, expedition, itinerary));
        }

      if (nextButton.isEnabled()) executor.executeScript("arguments[0].click();", nextButton);
      else hasNextYear = false;
    }
  }

  private void saveDeparture(
      WebElement element, String year, Expedition expedition, Itinerary itinerary) {
    String shipSelector = "div.flex > div > p.flex.pt-3";
    String startSelector = "p.text-lg";
    String endSelector = "div.flex.row > p.text-gray-500";
    String startingPriceSelector = "p > span.line-through";
    String discountedPriceSelector = "div.flex.items-baseline.pb-1 > h2.mr-1";

    Document doc = Jsoup.parse(element.getAttribute("innerHTML"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d yyyy", Locale.ENGLISH);

    String vesselName = doc.select(shipSelector).text();
    Optional<Vessel> vessel = vesselService.findByName(vesselName);
    if (vessel.isEmpty()) return;

    String startDate = doc.select(startSelector).text() + " " + year;
    String endDate = doc.select(endSelector).get(1).text().replace("Returning ", "") + " " + year;

    BigDecimal[] prices = extractPrices(doc, startingPriceSelector, discountedPriceSelector);

    departureService.saveDeparture(
        expedition,
        vessel.get(),
        itinerary,
        null,
        LocalDate.parse(startDate, formatter),
        LocalDate.parse(endDate, formatter),
        prices[0],
        prices[1],
        expedition.getWebsite());
  }

  private void scrapeExtensions(Expedition expedition) {
    String sectionSelector = "div[name='excursions']";
    String btnSelector = "a.aurora-button.aurora-button-color-dark";
    String extensionSelector = "a.flex.font-normal";
    String nameSelector = "h3";
    String imgSelector = "img";
    String priceSelector = "p.text-white.h3-text";
    String infoSelector = "div.infoblock > div.aurora-list-item.dd";

    WebElement a = findElement(sectionSelector);
    List<WebElement> btns = findElements(a, btnSelector);
    if (btns.isEmpty()) return;

    int lastIndex = btns.size() == 1 ? 0 : btns.size() - 1;
    navigateTo(btns.get(lastIndex).getAttribute("href"), extensionSelector);

    lazyLoadImages(extensionSelector, imgSelector);
    List<Element> elements =
        findElements(extensionSelector).stream()
            .map((x -> Jsoup.parse(x.getAttribute("outerHTML"))))
            .collect(Collectors.toList());

    for (Element element : elements) {
      String name = element.select(nameSelector).text();
      String website = cruiseLine.getWebsite() + element.select("a").attr("href");
      String photoUrl = getImageUrl(element.select(imgSelector).attr("src"));

      Optional<Extension> extension = extensionService.getExtension(cruiseLine, name);
      if (extension.isPresent()) {
        extensionService.saveExpeditionExtension(extension.get(), expedition);
        continue;
      }

      navigateTo(website);
      Document doc = getParsedPageSource();
      BigDecimal startingPrice = extractPrice(doc.select(priceSelector).text());
      int duration = Integer.parseInt(Objects.requireNonNull(doc.selectFirst(infoSelector)).text());

      extensionService.saveExtension(
          cruiseLine, expedition, name, startingPrice, duration, photoUrl, website);
    }
  }

  private void lazyLoadImages(String selector, String imageSelector) {
    for (WebElement item : findElements(selector)) lazyLoadImage(findElement(item, imageSelector));
  }

  private void lazyLoadImage(WebElement element) {
    getExecutor().executeScript("arguments[0].scrollIntoView(true);", element);
    wait.until(
        (WebDriver wd) -> {
          String currentSrc = element.getAttribute("src");
          return currentSrc != null && !currentSrc.contains("data:image/gif;base64");
        });
  }

  private String getImageUrl(String url) {
    return url.startsWith("https://") ? url : "https:" + url;
  }
}
