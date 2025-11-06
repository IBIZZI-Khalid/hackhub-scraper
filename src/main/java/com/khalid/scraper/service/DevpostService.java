package com.khalid.scraper.service;

import com.google.gson.*;
import com.khalid.scraper.model.HackathonDTO;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Service class for fetching hackathon data from Devpost API.
 * Includes retry logic with exponential backoff for rate limiting and server
 * errors.
 */
public class DevpostService {
    private static final String API_BASE_URL = "https://devpost.com/api/hackathons";
    private static final String USER_AGENT = "Mozilla/5.0 (HackHub Scraper)";
    private static final int TIMEOUT_SECONDS = 15;
    private static final int DELAY_MS = 1000;

    // Retry configuration
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_RETRY_DELAY_MS = 2000;
    private static final int MAX_RETRY_DELAY_MS = 30000;

    private final HttpClient httpClient;
    private final boolean debug;

    public DevpostService(boolean debug) {
        this.debug = debug;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Fetch hackathons from Devpost API with pagination.
     * 
     * @param maxPages Maximum number of pages to fetch (0 = all)
     * @return List of hackathon DTOs
     */
    public List<HackathonDTO> fetchHackathons(int maxPages) {
        List<HackathonDTO> allHackathons = new ArrayList<>();
        int currentPage = 1;
        int totalFetched = 0;

        log("Starting to fetch hackathons...");

        while (true) {
            if (maxPages > 0 && currentPage > maxPages) {
                log("Reached maximum page limit: " + maxPages);
                break;
            }

            try {
                System.out.print("📄 Fetching page " + currentPage + "... ");

                JsonObject response = fetchPageWithRetry(currentPage);
                JsonArray hackathonsArray = response.getAsJsonArray("hackathons");

                if (hackathonsArray == null || hackathonsArray.size() == 0) {
                    System.out.println("✓ No more data");
                    log("No more hackathons found. Ending pagination.");
                    break;
                }

                System.out.println("✓ Found " + hackathonsArray.size() + " hackathons");

                for (int i = 0; i < hackathonsArray.size(); i++) {
                    JsonObject hackathonJson = hackathonsArray.get(i).getAsJsonObject();
                    HackathonDTO dto = parseHackathon(hackathonJson);
                    allHackathons.add(dto);
                    totalFetched++;

                    if (debug) {
                        System.out.println("  ✓ " + dto.getTitle());
                    }
                }

                currentPage++;

                // Rate limiting: pause between requests
                if (currentPage <= maxPages || maxPages == 0) {
                    Thread.sleep(DELAY_MS);
                }

            } catch (InterruptedException e) {
                System.err.println("✗ Interrupted during fetch");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("✗ Error fetching page " + currentPage + ": " + e.getMessage());
                log("Fatal error: " + e.getClass().getName() + " - " + e.getMessage());
                break;
            }
        }

        System.out.println("\n✅ Total hackathons fetched: " + totalFetched);
        return allHackathons;
    }

    /**
     * Fetch a single page with retry logic and exponential backoff.
     */
    private JsonObject fetchPageWithRetry(int page) throws Exception {
        String url = API_BASE_URL + "?page=" + page;
        int attempt = 0;
        int retryDelay = INITIAL_RETRY_DELAY_MS;

        while (attempt <= MAX_RETRIES) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("User-Agent", USER_AGENT)
                        .header("Accept", "application/json")
                        .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                if (statusCode == 200) {
                    return JsonParser.parseString(response.body()).getAsJsonObject();
                } else if (statusCode == 429) {
                    // Rate limited
                    if (attempt < MAX_RETRIES) {
                        System.out.println("\n⚠️  Rate limited (429). Retrying in " + (retryDelay / 1000) + "s...");
                        Thread.sleep(retryDelay);
                        retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY_MS);
                        attempt++;
                    } else {
                        throw new IOException("Rate limit exceeded after " + MAX_RETRIES + " retries");
                    }
                } else if (statusCode >= 500) {
                    // Server error
                    if (attempt < MAX_RETRIES) {
                        System.out.println(
                                "\n⚠️  Server error (" + statusCode + "). Retrying in " + (retryDelay / 1000) + "s...");
                        Thread.sleep(retryDelay);
                        retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY_MS);
                        attempt++;
                    } else {
                        throw new IOException("Server error " + statusCode + " after " + MAX_RETRIES + " retries");
                    }
                } else {
                    throw new IOException("HTTP " + statusCode + ": " + response.body());
                }

            } catch (IOException | InterruptedException e) {
                if (attempt < MAX_RETRIES && !(e instanceof InterruptedException)) {
                    System.out.println("\n⚠️  Connection error. Retrying in " + (retryDelay / 1000) + "s...");
                    Thread.sleep(retryDelay);
                    retryDelay = Math.min(retryDelay * 2, MAX_RETRY_DELAY_MS);
                    attempt++;
                } else {
                    throw e;
                }
            }
        }

        throw new IOException("Failed to fetch page after " + MAX_RETRIES + " retries");
    }

    /**
     * Parse JSON object into HackathonDTO.
     */
    private HackathonDTO parseHackathon(JsonObject json) {
        HackathonDTO dto = new HackathonDTO();
        dto.setTitle(getStringOrNull(json, "title"));
        dto.setUrl(getStringOrNull(json, "url"));
        dto.setOrganization(getStringOrNull(json, "organization_name"));
        dto.setLocation(getStringOrNull(json, "location"));
        dto.setStartDate(getStringOrNull(json, "start_a"));
        dto.setEndDate(getStringOrNull(json, "end_a"));
        dto.setPrizeAmount(getStringOrNull(json, "prize_amount"));
        dto.setRegistrationsCount(getIntOrNull(json, "registrations_count"));
        dto.setFeatured(getBooleanOrFalse(json, "featured"));
        dto.setOpenState(getStringOrNull(json, "open_state"));
        dto.setThumbnailUrl(getStringOrNull(json, "thumbnail_url"));
        dto.setSource("devpost");
        return dto;
    }

    // JSON helper methods
    private String getStringOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : null;
    }

    private Integer getIntOrNull(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsInt() : null;
    }

    private Boolean getBooleanOrFalse(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsBoolean() : false;
    }

    private void log(String message) {
        if (debug) {
            System.out.println("[DEBUG] " + message);
        }
    }
}
