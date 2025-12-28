# Testing the Scraper API (Postman / Frontend)

This document explains how to call the backend scraper endpoint and how to enable CORS for frontend testing.

## Endpoint
- POST http://localhost:8080/api/scraper/devpost
- Content-Type: application/json
- Returns: JSON array of `Event` objects

## Request JSON (body)
- `domain` (string) — search term (optional; if omitted the search is broad)
- `location` (string) — location filter (optional)
- `count` (int) — number of events to return; controller defaults to `5` when <= 0

Example body (Postman raw JSON):

{
  "domain": "Java",
  "location": "Remote",
  "count": 10
}

Notes:
- `count` is number of events (items), not number of Devpost pages.
- If `location` is omitted, no location filtering is applied.

## Postman quick steps
1. Open Postman and create a new `Request`.
2. Set method `POST` and URL `http://localhost:8080/api/scraper/devpost`.
3. Under `Headers`, ensure `Content-Type: application/json`.
4. Under `Body` choose `raw` → `JSON` and paste the example JSON above (adjust `domain`, `location`, `count`).
5. Send the request. Inspect the response body for scraped events.

(Optional) Save the request into a collection so teammates can re-use it.

## cURL example

```bash
curl -X POST "http://localhost:8080/api/scraper/devpost" \
  -H "Content-Type: application/json" \
  -d '{"domain":"Java","location":"Remote","count":5}'
```

## Frontend (fetch) example

```javascript
fetch('http://localhost:8080/api/scraper/devpost', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ domain: 'Java', location: 'Remote', count: 5 })
})
.then(r => r.json())
.then(data => console.log(data))
.catch(err => console.error(err));
```

If you test from a browser, the server must allow CORS (see below).

## Enabling CORS for frontend testing
You can enable CORS either per-controller or globally.

Option A — allow all origins for this controller (quick, insecure, good for local testing):

In `ScraperController` add the annotation above the class or method:

```java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/scraper")
public class ScraperController { ... }
```

Option B — restrict to specific origin (recommended for dev):

```java
@CrossOrigin(origins = "http://localhost:3000") // replace with your frontend origin
@RestController
@RequestMapping("/api/scraper")
public class ScraperController { ... }
```

Option C — global CORS configuration (add a bean):

Create a config class (example `WebConfig`) in your Spring Boot app:

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
```

After changing CORS settings, rebuild and restart the backend (`mvn -DskipTests package` then `java -jar target/...jar` or run via your IDE).

## Defaults & behavior reminder
- The controller handles a JSON `ScrapeRequest` body with `domain`, `location`, and `count`.
- If `count` ≤ 0 the controller sets a default `count = 5`.
- `domain` and `location` are optional; leaving them blank returns a broader result set.

## Troubleshooting
- 405 / CORS error from browser: enable CORS as above.
- 404: ensure the backend runs on port 8080 and the endpoint path is correct.
- Empty response: try increasing `count` or check backend logs for scraping errors.

---
File: POSTMAN.md (in repo root).

## MLH endpoint example

- Endpoint: `POST http://localhost:8080/api/scraper/mlh`
- Body JSON fields: `domain` (string, optional), `location` (string, optional), `count` (int — number of events)

Example curl:

```bash
curl -X POST "http://localhost:8080/api/scraper/mlh" \
  -H "Content-Type: application/json" \
  -d '{"domain":"python","location":"remote","count":3}'
```

Fetch example for frontend:

```javascript
fetch('http://localhost:8080/api/scraper/mlh', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ domain: 'python', location: 'remote', count: 3 })
})
.then(r => r.json())
.then(data => console.log(data))
.catch(err => console.error(err));
```

Notes:
- Behaviour and defaults mirror the Devpost endpoint: `count` defaults to `5` when <= 0; `domain`/`location` are optional filters.
