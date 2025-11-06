package com.khalid.scraper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.khalid.scraper.config.ScraperConfig;
import com.khalid.scraper.model.HackathonDTO;
import com.khalid.scraper.service.DevpostService;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main CLI application for scraping hackathons from Devpost.
 * Production-ready with retry logic, configuration options, and Spring Boot integration support.
 * 
 * Usage:
 *   mvn exec:java
 *   mvn exec:java -Dexec.args="--pages=5"
 *   mvn exec:java -Dexec.args="--pages=10 --debug"
 *   mvn exec:java -Dexec.args="--output=custom.json --timestamp"
 */
public class DevpostScraper {
    
    private static final String VERSION = "2.0.0";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) {
        printBanner();
        
        try {
            // Parse configuration from CLI arguments
            ScraperConfig config = ScraperConfig.fromArgs(args);
            config.printConfig();

            // Initialize service layer
            DevpostService service = new DevpostService(config.isDebug());

            // Fetch hackathons with retry logic
            long startTime = System.currentTimeMillis();
            List<HackathonDTO> hackathons = service.fetchHackathons(config.getMaxPages());
            long duration = System.currentTimeMillis() - startTime;

            if (hackathons.isEmpty()) {
                System.out.println("\n⚠️  No hackathons found.");
                return;
            }

            // Save to JSON file
            saveToJson(hackathons, config.getOutputFile());
            
            // Print summary
            printSummary(hackathons, config.getOutputFile(), duration);
            
        } catch (Exception e) {
            System.err.println("\n❌ Fatal error: " + e.getMessage());
            if (ScraperConfig.fromArgs(args).isDebug()) {
                e.printStackTrace();
            }
            System.exit(1);
        }
    }

    /**
     * Save hackathons list to JSON file.
     */
    private static void saveToJson(List<HackathonDTO> hackathons, String filename) throws IOException {
        System.out.print("\n💾 Saving to " + filename + "... ");
        
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>) 
                    (src, typeOfSrc, context) -> new com.google.gson.JsonPrimitive(
                        src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .create();
        
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(hackathons, writer);
        }
        
        System.out.println("✓ Done");
    }

    /**
     * Print execution summary.
     */
    private static void printSummary(List<HackathonDTO> hackathons, String filename, long durationMs) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         SCRAPING COMPLETED             ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("📊 Total hackathons: " + hackathons.size());
        System.out.println("⏱️  Duration: " + (durationMs / 1000.0) + "s");
        System.out.println("📁 Output file: " + filename);
        System.out.println("🕒 Completed at: " + LocalDateTime.now().format(TIME_FORMAT));
        
        // Show sample of results
        System.out.println("\n📋 Sample results:");
        hackathons.stream()
                .limit(3)
                .forEach(h -> System.out.println("  • " + h.getTitle() + " (" + h.getOrganization() + ")"));
        
        if (hackathons.size() > 3) {
            System.out.println("  ... and " + (hackathons.size() - 3) + " more");
        }
        
        System.out.println("\n✅ Ready for Spring Boot integration!");
        System.out.println("💡 Tip: Use --timestamp flag to auto-timestamp output files");
    }

    /**
     * Print application banner.
     */
    private static void printBanner() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      HackHub Devpost Scraper          ║");
        System.out.println("║           Version " + VERSION + "                ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}