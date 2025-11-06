# Quick Start Guide - HackHub Devpost Scraper

## 🚀 Getting Started (60 seconds)

### Step 1: Clone & Build
```bash
cd hackhub_scraper_java
mvn clean compile
```

### Step 2: Run Your First Scrape
```bash
# Fetch first 3 pages
mvn exec:java -Dexec.args="--pages=3"
```

### Step 3: Check Output
```bash
# View results
cat hackathons.json
```

## 📋 Common Commands

```bash
# Fetch all hackathons (no limit)
mvn exec:java

# Fetch 5 pages with debug info
mvn exec:java -Dexec.args="--pages=5 --debug"

# Custom output with timestamp
mvn exec:java -Dexec.args="--output=export.json --timestamp"

# All options combined
mvn exec:java -Dexec.args="--pages=10 --output=data.json --timestamp --debug"
```

## 🎯 What You Get

**Input**: Devpost API (`https://devpost.com/api/hackathons`)  
**Output**: Clean JSON with all hackathon details  
**Format**: Ready for Spring Boot REST API integration

### Sample Output Structure:
```json
{
  "title": "Cloud Run Hackathon",
  "url": "https://run.devpost.com/",
  "organization": "Google",
  "prizeAmount": "$50,000",
  "registrationsCount": 2790,
  "featured": true,
  "openState": "open",
  "scrapedAt": "2025-11-06T15:18:36"
}
```

## 🔧 Configuration Options

| Flag | Example | Result |
|------|---------|--------|
| `--pages=N` | `--pages=5` | Fetch only 5 pages |
| `--output=FILE` | `--output=data.json` | Custom filename |
| `--timestamp` | `--timestamp` | Adds timestamp to filename |
| `--debug` | `--debug` | Shows detailed logs |

## 🏗️ Spring Boot Integration

### Quick Integration Example:

```java
@RestController
@RequestMapping("/api")
public class HackathonController {
    
    @PostMapping("/import-hackathons")
    public ResponseEntity<?> importHackathons() {
        DevpostService service = new DevpostService(false);
        List<HackathonDTO> hackathons = service.fetchHackathons(10);
        
        // Save to your database
        hackathonRepository.saveAll(hackathons);
        
        return ResponseEntity.ok(Map.of(
            "imported", hackathons.size(),
            "status", "success"
        ));
    }
}
```

## ✨ Key Features

✅ **Retry Logic** - Handles rate limits (429) and server errors (500+)  
✅ **Progress Indicators** - Real-time console feedback  
✅ **Exponential Backoff** - 2s → 4s → 8s delays on retries  
✅ **Production Ready** - Error handling, logging, clean architecture  
✅ **Modular Design** - Easy to integrate with Spring Boot

## 🐛 Troubleshooting

### Problem: Rate Limited
**Solution**: Increase delay in `DevpostService.java`
```java
private static final int DELAY_MS = 2000; // 2 seconds between pages
```

### Problem: Compilation Error
**Solution**: Clean and rebuild
```bash
mvn clean compile
```

### Problem: Connection Timeout
**Solution**: Check internet connection, retry will handle temporary issues

## 📊 Performance

- **Speed**: ~2-3 seconds per page
- **Reliability**: 3 retries with exponential backoff
- **Memory**: Low footprint, streams data page-by-page
- **Scalability**: Tested with 100+ pages

## 🎓 Usage Examples

### Example 1: Daily Data Sync
```bash
# Cron job at 2 AM daily
0 2 * * * cd /path/to/scraper && mvn exec:java -Dexec.args="--timestamp" >> scraper.log 2>&1
```

### Example 2: Quick Sample
```bash
# Get just 2 pages for testing
mvn exec:java -Dexec.args="--pages=2 --debug"
```

### Example 3: Full Export with Timestamp
```bash
# Complete export with dated filename
mvn exec:java -Dexec.args="--output=hackathons_export.json --timestamp"
# Creates: hackathons_export_2025-11-06_15-30-45.json
```

## 📦 Project Structure

```
src/main/java/com/khalid/scraper/
├── DevpostScraper.java       # CLI entry point
├── config/
│   └── ScraperConfig.java    # Configuration parser
├── model/
│   └── HackathonDTO.java     # Data model
└── service/
    └── DevpostService.java   # Core logic + retry
```

## 🔗 Next Steps

1. ✅ **Scraper works** - You're here!
2. 🔄 **Integrate with Spring Boot** - Copy service layer
3. 💾 **Add Database** - Save to MySQL/PostgreSQL
4. 🌐 **Create REST API** - Expose via endpoints
5. 🎨 **Build Frontend** - Display with React

## 💡 Pro Tips

1. Use `--debug` when developing to see detailed logs
2. Use `--timestamp` for archiving historical data
3. Set `--pages=2` for quick testing
4. Let it run without `--pages` for complete data
5. Check `README.md` for full documentation

## 🆘 Need Help?

- Check full documentation: `README.md`
- Enable debug mode: `--debug`
- Review error logs in terminal
- Verify API is accessible: `curl https://devpost.com/api/hackathons?page=1`

---

**Ready to integrate with your Spring Boot backend!** 🚀
