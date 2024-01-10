package com.antarctica.explorer.api.scraper;

import com.antarctica.explorer.api.model.CruiseLine;
import com.antarctica.explorer.api.service.CruiseLineService;
import com.antarctica.explorer.api.service.ExpeditionService;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class Scraper {
  protected WebDriver driver;
  protected WebDriverWait wait;
  protected CruiseLine cruiseLine;
  protected ExpeditionService expeditionService;

  public Scraper(
      CruiseLineService cruiseLineService, ExpeditionService expeditionService, String name) {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    this.driver = new ChromeDriver(options);

    this.wait = new WebDriverWait(driver, Duration.ofSeconds(60));

    this.cruiseLine =
        cruiseLineService
            .findByName(name)
            .orElseThrow(() -> new RuntimeException("CruiseLine \"" + name + "\" not found"));
    this.expeditionService = expeditionService;
  }

  protected void navigateTo(String website) {
    driver.get(website);
    System.out.println("Loaded website: " + website);
  }

  protected void waitForPresenceOfElement(By locator) {
    wait.until(ExpectedConditions.presenceOfElementLocated(locator));
  }

  protected void waitForInvisibilityOfElement(By locator) {
    wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
  }

  protected void quitDriver() {
    driver.quit();
  }

  public abstract void scrape();
}
