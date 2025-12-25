package com.hackhub.service.impl;

import com.hackhub.model.Event;
import com.hackhub.service.ScraperService;
import org.springframework.stereotype.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class DevpostScraperServiceImpl implements ScraperService {

    private static final String BASE_URL = "https://devpost.com/hackathons";

    @Override
    public List<Event> scrapeDevpost(String domain, String location, int count) {

        List<Event> events = new ArrayList<>();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        try {
            int page = 1;

            while (events.size() < count) {

                String searchUrl = BASE_URL + "?search=" +
                        (domain != null ? domain : "") + "&page=" + page;

                driver.get(searchUrl);

                Thread.sleep(3000); // allow JS tiles to load

                List<WebElement> tiles = driver.findElements(
                        By.cssSelector(".challenge-listing, .hackathon-tile"));

                if (tiles.isEmpty())
                    break;

                for (WebElement tile : tiles) {
                    if (events.size() >= count)
                        break;

                    Event event = parseEvent(tile, driver);

                    if (event == null)
                        continue;

                    if (location != null && !location.isBlank()) {
                        if (event.getLocation() == null ||
                                !event.getLocation().toLowerCase().contains(location.toLowerCase())) {
                            continue;
                        }
                    }

                    events.add(event);
                }

                page++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return events;
    }

    private Event parseEvent(WebElement tile, WebDriver driver) {

        try {
            Event event = new Event();

            // ===== TITLE =====
            try {
                WebElement titleEl = tile.findElement(By.cssSelector(".title, h3"));
                event.setTitle(titleEl.getText().trim());
            } catch (Exception e) {
                event.setTitle("Unknown Title");
            }

            // ===== URL =====
            try {
                WebElement linkEl = tile.findElement(By.cssSelector("a"));
                event.setUrl(linkEl.getAttribute("href"));
            } catch (Exception ignored) {
            }

            // ===== LOCATION =====
            try {
                WebElement locEl = tile.findElement(By.cssSelector(".location, .info"));
                event.setLocation(locEl.getText().trim());
            } catch (Exception e) {
                event.setLocation("Online");
            }

            // ===== SHORT BLURB (FROM TILE) =====
            try {
                WebElement blurbEl = tile.findElement(
                        By.cssSelector(".summary, .excerpt, .short-description"));
                event.setBlurb(blurbEl.getText().trim());
            } catch (Exception ignored) {
            }

            // ===== DETAIL PAGE SCRAPING (REAL FIX) =====
            if (event.getUrl() != null && !event.getUrl().isBlank()) {

                driver.get(event.getUrl());
                Thread.sleep(3000);

                // REAL DESCRIPTION (rendered)
                try {
                    WebElement desc = driver.findElement(
                            By.cssSelector("section#challenge-description"));
                    String html = desc.getAttribute("innerHTML");
                    if (html != null && !html.isBlank()) {
                        event.setDescription(html.trim());
                    } else {
                        event.setDescription(desc.getText().trim());
                    }
                } catch (Exception ignored) {
                }

                // REQUIREMENTS
                try {
                    WebElement req = driver.findElement(
                            By.cssSelector("section#challenge-requirements"));
                    event.setRequirements(req.getText().trim());
                } catch (Exception ignored) {
                }

                // JUDGING CRITERIA
                try {
                    WebElement crit = driver.findElement(
                            By.cssSelector("section#judging-criteria"));
                    event.setJudgingCriteria(crit.getText().trim());
                } catch (Exception ignored) {
                }

                // JUDGES
                try {
                    WebElement judges = driver.findElement(
                            By.cssSelector("section#judges"));
                    event.setJudges(judges.getText().trim());
                } catch (Exception ignored) {
                }
            }

            event.setProvider("DEVPOST");
            event.setType("HACKATHON");

            return event;

        } catch (Exception e) {
            return null;
        }
    }
}
