package com.hackhub.controller;

import com.hackhub.model.Event;
import com.hackhub.model.dto.ScrapeRequest;
import com.hackhub.service.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

    private final ScraperService scraperService;

    @Autowired
    public ScraperController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @PostMapping("/devpost")
    public ResponseEntity<List<Event>> scrapeDevpost(@RequestBody ScrapeRequest request) {
        // Apply defaults for null/empty parameters (and use local vars for logging)
        String effectiveDomain = (request.getDomain() == null) ? "" : request.getDomain();
        String effectiveLocation = (request.getLocation() == null) ? "" : request.getLocation();
        int effectiveCount = (request.getCount() <= 0) ? 10 : request.getCount();
        if (effectiveCount > 50)
            effectiveCount = 50;

        // Update request object for consistency
        request.setDomain(effectiveDomain);
        request.setLocation(effectiveLocation);
        request.setCount(effectiveCount);

        System.out.println("📥 [DEVPOST] Received scrape request: Domain='" + effectiveDomain
                + "', Location='" + effectiveLocation + "', Count=" + effectiveCount);

        List<Event> events = scraperService.scrapeDevpost(
                effectiveDomain,
                effectiveLocation,
                effectiveCount);

        return ResponseEntity.ok(events);
    }

    @PostMapping("/mlh")
    public ResponseEntity<List<Event>> scrapeMlh(@RequestBody ScrapeRequest request) {
        // Apply defaults for null/empty parameters (and use local vars for logging)
        String effectiveDomain = (request.getDomain() == null) ? "" : request.getDomain();
        String effectiveLocation = (request.getLocation() == null) ? "" : request.getLocation();
        int effectiveCount = (request.getCount() <= 0) ? 10 : request.getCount();
        if (effectiveCount > 50)
            effectiveCount = 50;

        // Update request object for consistency
        request.setDomain(effectiveDomain);
        request.setLocation(effectiveLocation);
        request.setCount(effectiveCount);

        System.out.println("📥 [MLH] Received scrape request: Domain='" + effectiveDomain
                + "', Location='" + effectiveLocation + "', Count=" + effectiveCount);

        List<Event> events = scraperService.scrapeMlh(effectiveDomain, effectiveLocation, effectiveCount);

        return ResponseEntity.ok(events);
    }

    private final java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newCachedThreadPool();

    @GetMapping("/stream/devpost")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamDevpost(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "10") int count) {

        System.out.println("\n🔍 [DEVPOST STREAM] Client connected - domain: " + domain + ", location: " + location + ", count: " + count);
        
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                300_000L); // 5 min timeout
        final java.util.concurrent.atomic.AtomicBoolean streamEnded = new java.util.concurrent.atomic.AtomicBoolean(false);
        final java.util.concurrent.atomic.AtomicInteger eventCount = new java.util.concurrent.atomic.AtomicInteger(0);

        executor.execute(() -> {
            try {
                scraperService.streamDevpost(domain, location, count, event -> {
                    if (!streamEnded.get()) {
                        try {
                            emitter.send(event);
                            int sent = eventCount.incrementAndGet();
                            System.out.println("✅ [DEVPOST STREAM] Event #" + sent + " sent: " + event.getTitle());
                        } catch (Exception e) {
                            System.err.println("❌ [DEVPOST STREAM] Send failed: " + e.getMessage());
                            streamEnded.set(true);
                            try { emitter.completeWithError(e); } catch (Exception ignored) {}
                        }
                    }
                }, () -> {
                    if (!streamEnded.get()) {
                        streamEnded.set(true);
                        System.out.println("✅ [DEVPOST STREAM] Completed - Total events sent: " + eventCount.get());
                        emitter.complete();
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ [DEVPOST STREAM] Error: " + e.getMessage());
                if (!streamEnded.get()) {
                    streamEnded.set(true);
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }
            }
        });

        return emitter;
    }

    @GetMapping("/stream/mlh")
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamMlh(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "10") int count) {

        System.out.println("\n🔍 [MLH STREAM] Client connected - domain: " + domain + ", location: " + location + ", count: " + count);
        
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(
                300_000L); // 5 min timeout
        final java.util.concurrent.atomic.AtomicBoolean streamEnded = new java.util.concurrent.atomic.AtomicBoolean(false);
        final java.util.concurrent.atomic.AtomicInteger eventCount = new java.util.concurrent.atomic.AtomicInteger(0);

        executor.execute(() -> {
            try {
                scraperService.streamMlh(domain, location, count, event -> {
                    if (!streamEnded.get()) {
                        try {
                            emitter.send(event);
                            int sent = eventCount.incrementAndGet();
                            System.out.println("✅ [MLH STREAM] Event #" + sent + " sent: " + event.getTitle());
                        } catch (Exception e) {
                            System.err.println("❌ [MLH STREAM] Send failed: " + e.getMessage());
                            streamEnded.set(true);
                            try { emitter.completeWithError(e); } catch (Exception ignored) {}
                        }
                    }
                }, () -> {
                    if (!streamEnded.get()) {
                        streamEnded.set(true);
                        System.out.println("✅ [MLH STREAM] Completed - Total events sent: " + eventCount.get());
                        emitter.complete();
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ [MLH STREAM] Error: " + e.getMessage());
                if (!streamEnded.get()) {
                    streamEnded.set(true);
                    try { emitter.completeWithError(e); } catch (Exception ignored) {}
                }
            }
        });

        return emitter;
    }
}
