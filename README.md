# HackHub Devpost Scraper

Production-ready Java scraper for fetching hackathon data from Devpost API.

## Features

✅ **API-Based Scraping** - Uses official Devpost JSON API  
✅ **Retry Logic** - Exponential backoff for 429 (rate limit) and 500+ (server errors)  
✅ **Pagination Support** - Automatic page fetching with configurable limits  
✅ **Progress Indicators** - Real-time console feedback with emojis  
✅ **Flexible Configuration** - CLI arguments for pages, output file, debug mode  
✅ **Timestamped Output** - Optional timestamp in filename  
✅ **Spring Boot Ready** - Modular service layer for easy integration  
✅ **Clean DTOs** - Structured data transfer objects for REST APIs

## Project Structure

```
src/main/java/com/khalid/scraper/
├── DevpostScraper.java          # Main CLI application
├── config/
│   └── ScraperConfig.java       # Configuration parser
├── model/
│   └── HackathonDTO.java        # Data transfer object
└── service/
    └── DevpostService.java      # Core scraping logic with retry
```

## Requirements

- Java 17+
- Maven 3.6+
- Internet connection

## Installation

```bash
git clone <repository-url>
cd hackhub_scraper_java
mvn clean install
```

## Usage

### Basic Usage

Fetch all hackathons:
```bash
mvn exec:java
```

### Limit Pages

Fetch first 5 pages:
```bash
mvn exec:java -Dexec.args="--pages=5"
```

### Custom Output File

Save to custom filename:
```bash
mvn exec:java -Dexec.args="--output=my_hackathons.json"
```

### Timestamped Output

Add timestamp to filename:
```bash
mvn exec:java -Dexec.args="--timestamp"
# Creates: hackathons_2025-11-06_15-30-45.json
```

### Debug Mode

Enable verbose logging:
```bash
mvn exec:java -Dexec.args="--debug"
```

### Combined Options

```bash
mvn exec:java -Dexec.args="--pages=10 --output=export.json --timestamp --debug"
```

## CLI Arguments

| Argument | Description | Default |
|----------|-------------|---------|
| `--pages=N` | Fetch first N pages (0 = all) | 0 (all) |
| `--output=FILE` | Output filename | hackathons.json |
| `--timestamp` | Add timestamp to filename | false |
| `--debug` | Enable debug logging | false |

## Output Format

JSON array of hackathon objects:

```json
[
  {
    "title": "Cloud Run Hackathon",
    "url": "https://run.devpost.com/",
    "organization": "Google",
    "location": "Online",
    "startDate": "2024-10-01",
    "endDate": "2024-12-01",
    "prizeAmount": "$50,000",
    "registrationsCount": 2790,
    "featured": true,
    "openState": "open",
    "thumbnailUrl": "//d112y698adiu2z.cloudfront.net/photos/...",
    "source": "devpost",
    "scrapedAt": "2025-11-06T15:16:20.876"
  }
]
```

## Spring Boot Integration

### 1. Add as Dependency

Copy the `model` and `service` packages to your Spring Boot project.

### 2. Create a REST Controller

```java
@RestController
@RequestMapping("/api/hackathons")
public class HackathonController {
    
    @Autowired
    private HackathonRepository repository;
    
    @PostMapping("/import")
    public ResponseEntity<ImportResult> importFromDevpost(
            @RequestParam(defaultValue = "0") int pages) {
        
        DevpostService service = new DevpostService(false);
        List<HackathonDTO> hackathons = service.fetchHackathons(pages);
        
        // Save to database
        List<Hackathon> saved = hackathons.stream()
            .map(this::convertToEntity)
            .map(repository::save)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(new ImportResult(saved.size()));
    }
}
```

### 3. Use as Scheduled Task

```java
@Component
public class HackathonSyncScheduler {
    
    @Autowired
    private HackathonRepository repository;
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void syncHackathons() {
        DevpostService service = new DevpostService(false);
        List<HackathonDTO> hackathons = service.fetchHackathons(0);
        
        // Update database
        hackathons.forEach(dto -> {
            repository.findByUrl(dto.getUrl())
                .ifPresentOrElse(
                    existing -> updateEntity(existing, dto),
                    () -> repository.save(convertToEntity(dto))
                );
        });
    }
}
```

## Retry Logic

The scraper implements exponential backoff:

- **Initial delay**: 2 seconds
- **Max delay**: 30 seconds
- **Max retries**: 3 attempts
- **Triggers**: HTTP 429 (rate limit), 500+ (server errors), connection errors

Example retry sequence:
1. Request fails → Wait 2s
2. Retry fails → Wait 4s
3. Retry fails → Wait 8s
4. Final attempt

## Rate Limiting

- 1 second delay between pages
- Configurable timeout: 15 seconds per request
- User-Agent: `Mozilla/5.0 (HackHub Scraper)`

## Error Handling

The scraper handles:
- ✅ Network timeouts
- ✅ Rate limiting (429)
- ✅ Server errors (500+)
- ✅ Invalid JSON responses
- ✅ Empty result sets
- ✅ Connection failures

## Development

### Run Tests
```bash
mvn test
```

### Build JAR
```bash
mvn package
java -jar target/hackhub_scraper_java-1.0-SNAPSHOT.jar --pages=5
```

### Debug Mode
Enable detailed logging to troubleshoot issues:
```bash
mvn exec:java -Dexec.args="--debug"
```

## API Endpoint

**Devpost API**: `https://devpost.com/api/hackathons?page=N`

Response structure:
```json
{
  "hackathons": [
    {
      "title": "...",
      "url": "...",
      "organization_name": "...",
      // ... more fields
    }
  ]
}
```

## Performance

- **Speed**: ~2 seconds per page (with 1s delay)
- **Memory**: Low footprint, processes page-by-page
- **Scalability**: Can fetch 100+ pages reliably
- **Resumable**: Each run is independent

## Troubleshooting

### Compilation Errors
```bash
mvn clean compile
```

### UTF-8 BOM Issues
Files are created with UTF-8 without BOM. If issues persist, verify file encoding.

### Rate Limiting
If frequently rate-limited, increase delay in `DevpostService.java`:
```java
private static final int DELAY_MS = 2000; // Increase to 2 seconds
```

## Future Enhancements

- [ ] Add filters (location, status, dates)
- [ ] Database integration module
- [ ] GraphQL API support
- [ ] Docker containerization
- [ ] Scheduled execution via cron
- [ ] Webhook notifications on completion
- [ ] Multi-source support (other platforms)

## License

MIT License - See LICENSE file for details

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## Authors

- **Khalid** - Initial work - HackHub Project

## Acknowledgments

- Devpost for providing the API
- Google Gson for JSON parsing
- Java HttpClient for HTTP requests
