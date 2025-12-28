package com.hackhub.service;

import com.hackhub.model.Event;
import com.hackhub.service.impl.ScraperServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ScraperServiceTest {

    @Autowired
    private ScraperServiceImpl scraperService;

    @Test
    public void testScrapeDevpost() {
        // Basic connectivity test
        List<Event> events = scraperService.scrapeDevpost("Java", "", 1);
        System.out.println("Devpost Events Found: " + events.size());
        if (!events.isEmpty()) {
            System.out.println("First Event: " + events.get(0).getTitle());
        }
        assertNotNull(events);
    }

    @Test
    public void testScrapeMlh() {
        // Test MLH scraping logic
        // Using "hackathon" generic query or empty to just get list
        List<Event> events = scraperService.scrapeMlh("", "", 1);

        System.out.println("MLH Events Found: " + events.size());
        if (!events.isEmpty()) {
            Event e = events.get(0);
            System.out.println("First MLH Event: " + e.getTitle());
            System.out.println("URL: " + e.getUrl());
            System.out.println("Desc length: " + (e.getDescription() != null ? e.getDescription().length() : "null"));
        }

        assertNotNull(events);
    }
}
