package com.hackhub.model.dto;

import lombok.Data;

@Data
public class ScrapeRequest {
    private String domain;   // e.g., "Java", "Python"
    private String location; // e.g., "New York", "Remote"
    private int count;       // e.g., 5
}
