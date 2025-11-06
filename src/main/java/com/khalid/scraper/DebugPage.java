package com.khalid.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.FileWriter;

public class DebugPage {
    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.connect("https://devpost.com/hackathons")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(15000)
                .get();

        try (FileWriter writer = new FileWriter("debug_page.html")) {
            writer.write(doc.html());
        }

        System.out.println("Saved HTML to debug_page.html");
        System.out.println("\n=== Link Analysis ===");
        System.out.println("Total links: " + doc.select("a").size());

        Elements devpostLinks = doc.select("a[href*='devpost.com']");
        System.out.println("Links with 'devpost.com': " + devpostLinks.size());

        System.out.println("\n=== First 10 Devpost Links ===");
        int count = 0;
        for (Element link : devpostLinks) {
            if (count++ >= 10)
                break;
            String href = link.attr("href");
            String text = link.text().trim();
            System.out.println(
                    count + ". " + href + " | Text: " + (text.length() > 50 ? text.substring(0, 50) + "..." : text));
        }

        // Check for subdomain patterns
        System.out.println("\n=== Subdomain Links (likely hackathons) ===");
        count = 0;
        for (Element link : devpostLinks) {
            String href = link.attr("href");
            if (href.matches("https?://[a-z0-9-]+\\.devpost\\.com/?.*") &&
                    !href.contains("help.") && !href.contains("info.") && !href.contains("secure.")) {
                if (count++ >= 10)
                    break;
                System.out.println(count + ". " + href + " | " + link.text());
            }
        }
    }
}
