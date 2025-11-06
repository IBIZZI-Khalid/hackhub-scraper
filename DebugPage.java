import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
        System.out.println("Saved to debug_page.html");
        System.out.println("Total links: " + doc.select("a").size());
        System.out.println("Links with 'devpost.com': " + doc.select("a[href*='devpost.com']").size());
    }
}
