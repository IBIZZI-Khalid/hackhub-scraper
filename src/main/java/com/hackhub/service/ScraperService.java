package com.hackhub.service;

import com.hackhub.model.Event;
import java.io.IOException;
import java.util.List;

public interface ScraperService {
    /**
     * Scrapes Devpost for hackathons based on criteria.
     * 
     * @param domain   The topic or technology (e.g., "Java").
     * @param location The location filter (e.g., "New York").
     * @param count    The minimum number of events to retrieve.
     * @return List of scraped events.
     */
    List<Event> scrapeDevpost(String domain, String location, int count);

    /**
     * Scrapes MLH for hackathons and fetches external details.
     * 
     * @param domain   The search term (filtered by title).
     * @param location The location filter.
     * @param count    The number of events to retrieve.
     * @return List of scraped events with external details.
     */
    List<Event> scrapeMlh(String domain, String location, int count);

    /**
     * Streams Devpost events in real-time.
     * 
     * @param onEvent    Callback for each new event found.
     * @param onComplete Callback when scraping is finished.
     */
    void streamDevpost(String domain, String location, int count, java.util.function.Consumer<Event> onEvent,
            Runnable onComplete);

    /**
     * Streams MLH events in real-time.
     * 
     * @param onEvent    Callback for each new event found.
     * @param onComplete Callback when scraping is finished.
     */
    void streamMlh(String domain, String location, int count, java.util.function.Consumer<Event> onEvent,
            Runnable onComplete);
}
