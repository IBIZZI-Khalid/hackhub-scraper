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
        // Try common short blurb fields from Devpost API
        String desc = getFirstString(json, "short_description", "description", "blurb", "summary");
        String requirements = getFirstString(json, "requirements", "challenge_requirements", "requirements_text");
        String judges = getFirstString(json, "judges", "judge_list");
        String judgingCriteria = getFirstString(json, "judging_criteria", "criteria", "judging");

        // If API did not provide some of these, attempt to fetch them from the
        // hackathon page
        Map<String, String> details = new HashMap<>();
        if ((desc == null || desc.isBlank() || requirements == null || judges == null || judgingCriteria == null)
                && dto.getUrl() != null && !dto.getUrl().isBlank()) {
            try {
                details = fetchDetailsFromPage(dto.getUrl());
                // prefer plain/text short description for blurb, avoid using HTML
                if ((desc == null || desc.isBlank()) && details.get("description") != null)
                    desc = details.get("description");
                if ((requirements == null || requirements.isBlank()) && details.get("requirements") != null)
                    requirements = details.get("requirements");
                if ((judges == null || judges.isBlank()) && details.get("judges") != null)
                    judges = details.get("judges");
                if ((judgingCriteria == null || judgingCriteria.isBlank()) && details.get("judgingCriteria") != null)
                    judgingCriteria = details.get("judgingCriteria");
            } catch (Exception e) {
                log("Could not fetch page details: " + e.getMessage());
            }
        }

        // Ensure we never leave fields null so they appear in JSON (empty string if
        // missing)
        dto.setBlurb(desc != null ? desc : "");
        dto.setRequirements(requirements != null ? requirements : "");
        dto.setJudges(judges != null ? judges : "");
        dto.setJudgingCriteria(judgingCriteria != null ? judgingCriteria : "");
        // set long description (prefer HTML from page), keep blurb as short text
        String longDesc = details.getOrDefault("descriptionHtml", details.getOrDefault("description", ""));
        dto.setDescription(longDesc);
        dto.setSource("devpost");
        return dto;
    }

    /**
     * Fetch the hackathon page and attempt to extract a short description or meta
     * description.
     */
    private String fetchDescriptionFromPage(String url) {
        try {
            // Use Jsoup to fetch and parse page HTML
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_SECONDS * 1000)
                    .get();

            // 1) meta description
            org.jsoup.nodes.Element meta = doc.selectFirst("meta[name=description], meta[property=og:description]");
            if (meta != null) {
                String content = meta.hasAttr("content") ? meta.attr("content") : meta.text();
                if (content != null && !content.isBlank())
                    return content.trim();
            }

            // 2) common page selectors for Devpost challenge blurb
            org.jsoup.nodes.Element blurb = doc.selectFirst(
                    ".challenge-blurb, .challenge-description, .blurb, .summary, .excerpt, .challenge-intro");
            if (blurb != null) {
                String text = blurb.text();
                if (text != null && !text.isBlank())
                    return text.trim();
            }

        } catch (Exception e) {
            // allow caller to handle/log
            if (debug)
                e.printStackTrace();
        }
        return null;
    }

    /**
     * Fetch multiple detail fields from the challenge page using Jsoup.
     */
    private Map<String, String> fetchDetailsFromPage(String url) {
        Map<String, String> out = new HashMap<>();
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(TIMEOUT_SECONDS * 1000)
                    .get();

            // description (meta or article element)
            org.jsoup.nodes.Element meta = doc.selectFirst("meta[name=description], meta[property=og:description]");
            if (meta != null) {
                String content = meta.hasAttr("content") ? meta.attr("content") : meta.text();
                if (content != null && !content.isBlank())
                    out.put("description", content.trim());
            }

            org.jsoup.nodes.Element descEl = doc.selectFirst(
                    "main #challenge-description, main .challenge-blurb, main #challenge-description, #challenge-description, .challenge-description");
            if (descEl != null) {
                String text = descEl.text();
                String html = descEl.html();
                if (text != null && !text.isBlank()) {
                    out.putIfAbsent("description", text.trim());
                }
                if (html != null && !html.isBlank()) {
                    out.putIfAbsent("descriptionHtml", html.trim());
                }
            }

            org.jsoup.nodes.Element req = doc.selectFirst(
                    "main #challenge-requirements, #challenge-requirements, .challenge-requirements, .requirements");
            if (req != null && !req.text().isBlank())
                out.put("requirements", req.text().trim());

            org.jsoup.nodes.Element judgesEl = doc.selectFirst("main #judges, #judges, .judges, .judge-list");
            if (judgesEl != null && !judgesEl.text().isBlank())
                out.put("judges", judgesEl.text().trim());

            org.jsoup.nodes.Element crit = doc
                    .selectFirst("main #judging-criteria, #judging-criteria, .judging-criteria, .criteria");
            if (crit != null && !crit.text().isBlank())
                out.put("judgingCriteria", crit.text().trim());

        } catch (Exception e) {
            if (debug)
                e.printStackTrace();
        }
        return out;
    }

    /**
     * Return the first non-null string value for the provided keys.
     */
    private String getFirstString(JsonObject json, String... keys) {
        for (String k : keys) {
            String v = getStringOrNull(json, k);
            if (v != null && !v.isEmpty())
                return v;
        }
        return null;
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
