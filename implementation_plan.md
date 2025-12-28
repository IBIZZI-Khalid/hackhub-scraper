# Implementation Plan - Phase 1: The Crawler

## Goal Description
Initialize the Spring Boot project and implement the "Module d'Acquisition de Données" (Web Scraping) as per the "Cahier des Charges". The goal is to scrape hackathon data from MLH, Devpost, and Hackerearth using a hybrid Jsoup/Selenium approach and store it in MySQL.

## User Review Required
> [!IMPORTANT]
> **Database Change**: The Python script used MongoDB. The PDF requires **MySQL** with JPA/Hibernate. We must ensure a MySQL server is available or use H2 for local testing initially.

> [!WARNING]
> **Performance**: Selenium is resource-heavy. We will implement it in "Headless" mode as requested, but we should prioritize Jsoup for sites that support it (likely MLH).

## Proposed Changes

### Project Structure (Spring Boot)
#### [NEW] [pom.xml](file:///c:/Users/hp/Desktop/hackhub_scraper_java/pom.xml)
- Add dependencies: `spring-boot-starter-data-jpa`, `spring-boot-starter-web`, `mysql-connector-j`, `jsoup`, `selenium-java`, `lombok`.

#### [NEW] [application.properties](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/resources/application.properties)
- Configure MySQL connection and JPA settings.
- Configure Selenium/WebDriver path if necessary (or use WebDriverManager).

### Domain Layer (Entities)
#### [NEW] [Event.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/model/Event.java)
- `@Entity` class with fields: `id`, `title`, `description`, `date`, `price`, `location`, `url`, `organizer`, `provider`.
- Enum `EventType` (HACKATHON, CERTIFICATION).

### Repository Layer
#### [NEW] [EventRepository.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/repository/EventRepository.java)
- Extends `JpaRepository<Event, Long>`.

### Scraping Layer
#### [NEW] [ScraperService.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/service/scraper/ScraperService.java)
- Interface with method `List<Event> scrape()`.

#### [NEW] [MlhScraper.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/service/scraper/MlhScraper.java)
- Implementation using **Jsoup** (likely static content).

#### [NEW] [DevpostScraper.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/service/scraper/DevpostScraper.java)
- Implementation using **Jsoup** (if possible) or **Selenium**.

#### [NEW] [HackerearthScraper.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/service/scraper/HackerearthScraper.java)
- Implementation using **Selenium** (likely dynamic).

#### [NEW] [ScraperScheduler.java](file:///c:/Users/hp/Desktop/hackhub_scraper_java/src/main/java/com/hackhub/service/scheduler/ScraperScheduler.java)
- `@Scheduled` methods to trigger scrapers.

## Verification Plan

### Automated Tests
- Create JUnit tests for `EventRepository` to verify database persistence.
- Create simple Integration Test for Scrapers (mocking the HTTP response or using a small live test).

### Manual Verification
- Run the `ScraperScheduler` manually on startup.
- Check MySQL database table `event` to see if records are populated.
