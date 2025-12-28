package com.hackhub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String blurb;

    private String url;

    private String location;

    private String date; // Keep as string for flexibility in scraping, can parse later

    private String imageUrl;

    private String provider; // e.g., "DEVPOST", "MLH"
    private String requirements;
    private String judges;
    private String judgingCriteria;
    private String type;

    private LocalDateTime scrappedAt;

    @PrePersist
    protected void onCreate() {
        scrappedAt = LocalDateTime.now();
    }
}
