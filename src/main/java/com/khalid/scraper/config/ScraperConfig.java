package com.khalid.scraper.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Configuration class for scraper CLI arguments and defaults.
 */
public class ScraperConfig {
    private int maxPages;
    private String outputFile;
    private boolean debug;
    private boolean useTimestamp;

    private static final String DEFAULT_OUTPUT = "hackathons.json";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public ScraperConfig() {
        this.maxPages = 0; // 0 means fetch all
        this.outputFile = DEFAULT_OUTPUT;
        this.debug = false;
        this.useTimestamp = false;
    }

    /**
     * Parse command line arguments and configure the scraper.
     */
    public static ScraperConfig fromArgs(String[] args) {
        ScraperConfig config = new ScraperConfig();
        
        for (String arg : args) {
            if (arg.startsWith("--pages=")) {
                config.maxPages = Integer.parseInt(arg.substring(8));
            } else if (arg.startsWith("--output=")) {
                config.outputFile = arg.substring(9);
            } else if (arg.equals("--debug")) {
                config.debug = true;
            } else if (arg.equals("--timestamp")) {
                config.useTimestamp = true;
            }
        }

        // Apply timestamp to filename if requested
        if (config.useTimestamp) {
            config.outputFile = addTimestampToFilename(config.outputFile);
        }

        return config;
    }

    private static String addTimestampToFilename(String filename) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0) {
            return filename.substring(0, dotIndex) + "_" + timestamp + filename.substring(dotIndex);
        }
        return filename + "_" + timestamp;
    }

    // Getters
    public int getMaxPages() { return maxPages; }
    public String getOutputFile() { return outputFile; }
    public boolean isDebug() { return debug; }
    public boolean isUseTimestamp() { return useTimestamp; }

    public void printConfig() {
        System.out.println("=== Scraper Configuration ===");
        System.out.println("Max Pages: " + (maxPages == 0 ? "All" : maxPages));
        System.out.println("Output File: " + outputFile);
        System.out.println("Debug Mode: " + (debug ? "ON" : "OFF"));
        System.out.println("============================\n");
    }
}
