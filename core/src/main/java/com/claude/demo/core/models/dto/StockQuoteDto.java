package com.claude.demo.core.models.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockQuoteDto(
    String symbol,
    double price,
    double change,
    double changePercent,
    String timestamp
) {}
