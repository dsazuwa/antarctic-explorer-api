package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.pojo.LindbladHit;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class LindbladScraper extends Scraper {
  private static final String ALGOLIA_URL =
      "https://prru6fnc68-dsn.algolia.net/1/indexes/*/queries";
  private static final String FORM_DATA =
      "{\"requests\":[{\"indexName\":\"prod_seaware_EXPEDITIONS\",\"params\":\"analytics=true&clickAnalytics=true&enablePersonalization=true&facetFilters=%5B%5B%22destinations.name%3AAntarctica%22%5D%5D&facets=%5B%22departureDates.dateFromTimestamp%22%2C%22destinations.name%22%2C%22ships.name%22%2C%22duration%22%2C%22productType%22%5D&filters=(nrDepartures%20%3E%200)%20AND%20(departureDates.dateFromTimestamp%20%3E%201704239999)&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&maxValuesPerFacet=40&numericFilters=%5B%22departureDates.dateFromTimestamp%3E%3D0%22%2C%22departureDates.dateFromTimestamp%3C%3D9999999999%22%5D&page=0&tagFilters=&userToken=00000000-0000-0000-0000-000000000000\"},{\"indexName\":\"prod_seaware_EXPEDITIONS\",\"params\":\"analytics=false&clickAnalytics=false&enablePersonalization=true&facets=destinations.name&filters=(nrDepartures%20%3E%200)%20AND%20(departureDates.dateFromTimestamp%20%3E%201704239999)&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&hitsPerPage=0&maxValuesPerFacet=40&numericFilters=%5B%22departureDates.dateFromTimestamp%3E%3D0%22%2C%22departureDates.dateFromTimestamp%3C%3D9999999999%22%5D&page=0&userToken=00000000-0000-0000-0000-000000000000\"},{\"indexName\":\"prod_seaware_EXPEDITIONS\",\"params\":\"analytics=false&clickAnalytics=false&enablePersonalization=true&facetFilters=%5B%5B%22destinations.name%3AAntarctica%22%5D%5D&facets=departureDates.dateFromTimestamp&filters=(nrDepartures%20%3E%200)%20AND%20(departureDates.dateFromTimestamp%20%3E%201704239999)&highlightPostTag=%3C%2Fais-highlight-0000000000%3E&highlightPreTag=%3Cais-highlight-0000000000%3E&hitsPerPage=0&maxValuesPerFacet=40&page=0&userToken=00000000-0000-0000-0000-000000000000\"}]}";
  private static final String DESCRIPTION_SELECTOR =
      "div.sc-c71aec9f-2.dVGsho > p.sc-1a030b44-1.ka-dLeA";

  private final CloseableHttpClient httpClient;
  private final ObjectMapper objectMapper;

  private boolean cookieAccepted = false;

  public LindbladScraper(CruiseLineService cruiseLineService, ExpeditionService expeditionService) {
    super(cruiseLineService, expeditionService, "Lindblad Expeditions");

    this.httpClient = HttpClients.createDefault();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void scrape() {
    try {
      HttpPost httpPost = createHttpPost();
      HttpResponse response = httpClient.execute(httpPost);

      handleResponse(response);

      HttpEntity entity = response.getEntity();
      LindbladHit[] hits = processResponseBody(entity);

      processHits(hits);

      httpClient.close();
    } catch (IOException e) {
      e.printStackTrace();
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
      if (!cookieAccepted) acceptCookie();

      Document doc = getParsedPageSource();

      String description = doc.select(DESCRIPTION_SELECTOR).text();
      String[] ports = extractPorts(doc);

      saveExpedition(hit, website, description, ports);
    }
  }

  private void acceptCookie() {
    String cookieSelector = "button.sc-baf605bd-1.hOSsqd";

    waitForPresenceOfElement(cookieSelector);
    WebElement acceptCookieButton = findElement(cookieSelector);
    acceptCookieButton.click();
    cookieAccepted = true;
  }

  private String[] extractPorts(Document doc) {
    String portSelector =
        "div.sc-12a2b3de-0.kCEMBM > div.sc-12a2b3de-1.fnHsb > span.sc-12a2b3de-3.cvVhAe";

    waitForPresenceOfElement(portSelector);
    String[] ports = doc.select(portSelector).stream().map(Element::text).toArray(String[]::new);

    if (ports.length != 2) throw new NoSuchElementException("Ports not found");
    return ports;
  }

  private void saveExpedition(LindbladHit hit, String website, String description, String[] ports) {
    expeditionService.saveIfNotExist(
        cruiseLine,
        website,
        hit.name,
        description,
        ports[0],
        ports[1],
        hit.durationUS + " days",
        new BigDecimal(hit.priceFromUSD),
        hit.thumbnail);
  }
}
