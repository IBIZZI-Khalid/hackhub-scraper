# HackHub Scraper & Recommendation Engine

## Phase 1: Core Scraper Implementation
- [ ] Verify & Upgrade Spring Boot Project Structure (Folders/Packages) <!-- id: 0 -->
- [ ] Create `Event` Entity and Repository (MySQL) <!-- id: 1 -->
- [ ] Implement `ScraperService` Interface <!-- id: 2 -->
- [ ] Implement `DevpostScraper` using Jsoup (Try Jsoup first) <!-- id: 3 -->
    - [ ] Handle Inputs: Location, Domain, Count
    - [ ] Implement Header Rotation (like Python script)
    - [ ] Implement Pagination to reach 'Count'
- [ ] Create `ScraperController` for On-Demand Scraping API <!-- id: 4 -->
- [ ] Verify Scraper Output (JSON/DB) <!-- id: 5 -->

## Phase 2: Advanced Scrapers (If Jsoup fails)
- [ ] Add Selenium to project <!-- id: 6 -->
- [ ] Refactor Devpost Scraper to use Selenium if needed <!-- id: 7 -->

## Phase 3: Intelligence & "Nearest" Logic (Future)
- [ ] Implement "Nearest" sorting logic in Java if API doesnt support it directly <!-- id: 8 -->

