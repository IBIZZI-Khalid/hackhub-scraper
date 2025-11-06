package com.khalid.scraper.model;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Hackathon data.
 * Ready for Spring Boot integration and REST API usage.
 */
public class HackathonDTO {
    private String title;
    private String url;
    private String organization;
    private String location;
    private String startDate;
    private String endDate;
    private String prizeAmount;
    private Integer registrationsCount;
    private Boolean featured;
    private String openState;
    private String thumbnailUrl;
    private String source;
    private LocalDateTime scrapedAt;

    public HackathonDTO() {
        this.scrapedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getPrizeAmount() { return prizeAmount; }
    public void setPrizeAmount(String prizeAmount) { this.prizeAmount = prizeAmount; }

    public Integer getRegistrationsCount() { return registrationsCount; }
    public void setRegistrationsCount(Integer registrationsCount) { this.registrationsCount = registrationsCount; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public String getOpenState() { return openState; }
    public void setOpenState(String openState) { this.openState = openState; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getScrapedAt() { return scrapedAt; }
    public void setScrapedAt(LocalDateTime scrapedAt) { this.scrapedAt = scrapedAt; }

    @Override
    public String toString() {
        return "HackathonDTO{" +
                "title='" + title + '\'' +
                ", organization='" + organization + '\'' +
                ", openState='" + openState + '\'' +
                '}';
    }
}
