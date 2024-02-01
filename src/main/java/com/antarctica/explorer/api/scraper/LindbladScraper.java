package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.Expedition;
import com.antarctica.explorer.api.pojo.LindbladHit;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

  private static final String ALGOLIA_URL =
      "https://prru6fnc68-dsn.algolia.net/1/indexes/*/queries";
  private static final String FORM_DATA =
      "{\"requests\":[{\"indexName\":\"prod_seaware_EXPEDITIONS\",\"params\":\"analytics=true&clickAnalytics=true&enablePersonalization=true&facetFilters=%5B%5B%22destinations.name%3AAntarctica%22%5D%5D&facets=%5B%22departureDates.dateFromTimestamp%22%2C%22destinations.name%22%2C%22ships.name%22%2C%22duration%22%2C%22productType%22%5D&filters=(nrDepartures%20%3E%200)%20AND%20(departureDates.dateFromTimestamp%20%3E%201704239999)&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&maxValuesPerFacet=40&numericFilters=%5B%22departureDates.dateFromTimestamp%3E%3D0%22%2C%22departureDates.dateFromTimestamp%3C%3D9999999999%22%5D&page=0&tagFilters=&userToken=00000000-0000-0000-0000-000000000000\"},{\"indexName\":\"prod_seaware_EXPEDITIONS\",\"params\":\"analytics=false&clickAnalytics=false&enablePersonalization=true&facets=destinations.name&filters=(nrDepartures%20%3E%200)%20AND%20(departureDates.dateFromTimestamp%20%3E%201704239999)&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&hitsPerPage=0&maxValuesPerFacet=40&numericFilters=%5B%22departureDates.dateFromTimestamp%3E%3D0%22%2C%22departureDates.dateFromTimestamp%3C%3D9999999999%22%5D&page=0&userToken=00000000-0000-0000-0000-000000000000\"},{\"indexName\":\"prod_seaware_EXPEDITIONS\",\"params\":\"analytics=false&clickAnalytics=false&enablePersonalization=true&facetFilters=%5B%5B%22destinations.name%3AAntarctica%22%5D%5D&facets=departureDates.dateFromTimestamp&filters=(nrDepartures%20%3E%200)%20AND%20(departureDates.dateFromTimestamp%20%3E%201704239999)&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&hitsPerPage=0&maxValuesPerFacet=40&page=0&userToken=00000000-0000-0000-0000-000000000000\"}]}";
  private static final String DESCRIPTION_SELECTOR =
      "div.sc-c71aec9f-2.dVGsho > p.sc-1a030b44-1.ka-dLeA";
  private static final String PORT_SELECTOR =
      "div.sc-36842228-0.Anwop.sc-d6abfba5-0.euRrRz > header.sc-36842228-9.dFwmLF > h3.sc-36842228-12.jKNlCi > div.sc-12a2b3de-0.kCEMBM > div.sc-12a2b3de-1.fnHsb > span.sc-12a2b3de-3.cvVhAe";
  private static final String HIGHLIGHT_SELECTOR = "ul.sc-d5179a42-0.jKPGuL > li > p";
  private static final String ITINERARY_SELECTOR =
      "div.sc-36842228-3.hBGyOc > div.sc-ad096f17-1.ebIpYW";
  private static final String DEPARTURE_SELECTOR = "ol.sc-487915d1-0.mwJrq > li";

  private final CloseableHttpClient httpClient;
  private final ObjectMapper objectMapper;

  private boolean cookieAccepted;
  private boolean newsletterRemoved;

  public LindbladScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(
        cruiseLineService,
        expeditionService,
        CRUISE_LINE_NAME,
        CRUISE_LINE_WEBSITE,
        EXPEDITION_WEBSITE,
        LOGO_URL);

    this.httpClient = HttpClients.createDefault();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void scrape() {
    try {
      cookieAccepted = false;
      newsletterRemoved = false;
      HttpPost httpPost = createHttpPost();
      HttpResponse response = httpClient.execute(httpPost);

      handleResponse(response);

      HttpEntity entity = response.getEntity();
      LindbladHit[] hits = processResponseBody(entity);

      processHits(hits);

      httpClient.close();
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } finally {
      quitDriver();
    }
  }

  @Override
  protected String getCurrentPageText() {
    return "1";
  }

  private HttpPost createHttpPost() throws UnsupportedEncodingException {
    HttpPost httpPost = new HttpPost(ALGOLIA_URL);

    httpPost.addHeader(
        "x-algolia-agent",
        "Algolia for JavaScript (4.17.1); Browser (lite); JS Helper (3.13.0); react (18.2.0); react-instantsearch (6.40.0)");
    httpPost.addHeader("x-algolia-api-key", "d116310485a589ce5aa40c286ec9bfe6");
    httpPost.addHeader("x-algolia-application-id", "PRRU6FNC68");
    httpPost.addHeader("Content-Type", "application/json");
    httpPost.setEntity(new StringEntity(FORM_DATA));

    return httpPost;
  }

  private void handleResponse(HttpResponse response) throws IOException {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode != 200) throw new IOException("Request failed with status code: " + statusCode);
  }

  private LindbladHit[] processResponseBody(HttpEntity entity) throws IOException {
    String responseBody = EntityUtils.toString(entity);

    return Optional.ofNullable(objectMapper.readTree(responseBody).get("results"))
        .filter(JsonNode::isArray)
        .map(results -> results.get(0).get("hits"))
        .map(JsonNode::toString)
        .map(this::deserializeHits)
        .orElse(new LindbladHit[0]);
  }

  private LindbladHit[] deserializeHits(String jsonString) {
    try {
      return objectMapper.readValue(jsonString, LindbladHit[].class);
    } catch (JsonProcessingException e) {
      return new LindbladHit[0];
    }
  }

  private void processHits(LindbladHit[] hits) {
    for (LindbladHit hit : hits) {
      String website = cruiseLine.getWebsite() + "/en/expeditions/" + hit.pageSlug;
      navigateTo(website, DESCRIPTION_SELECTOR);
      waitForPresenceOfElement(PORT_SELECTOR);
      waitForPresenceOfElement(ITINERARY_SELECTOR);
      waitForPresenceOfElement(DEPARTURE_SELECTOR);

      if (!cookieAccepted) acceptCookie();
      if (!newsletterRemoved) removeNewsletter();

      Document doc = getDocument();

      Expedition expedition = scrapeExpedition(doc, hit, website);
      scrapeItineraries(doc, expedition);
      scrapeDepartures(doc, expedition);
    }
  }

  private void acceptCookie() {
    String cookieSelector = "button.sc-baf605bd-1.hOSsqd";

    waitForPresenceOfElement(cookieSelector);
    WebElement acceptCookieButton = findElement(cookieSelector);
    acceptCookieButton.click();
    cookieAccepted = true;
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

  private Document getDocument() {
    String buttonSelector = "ol[data-module=departureCardList] > button.sc-baf605bd-1.cwYkyy";
    String lastDepartureSelector = "(//ol[@data-module='departureCardList']/li)[last()]";
    String newsLetterSelector = "section[aria-label='Newsletter sign up']";

    while (true) {
      WebElement button = findElement(buttonSelector);
      if (button == null) break;

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

  private Expedition scrapeExpedition(Document doc, LindbladHit hit, String website) {
    String description = doc.select(DESCRIPTION_SELECTOR).text();
    String[] ports = extractPorts(doc);
    String[] highlights =
        doc.select(HIGHLIGHT_SELECTOR).stream().map(Element::text).toArray(String[]::new);

    return expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        hit.name,
        description,
        ports[0],
        ports[1],
        hit.durationUS + "",
        new BigDecimal(hit.priceFromUSD),
        hit.thumbnail);
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
            ports,
            dates,
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
