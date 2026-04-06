package com.claude.demo.core.services;

import com.claude.demo.core.models.dto.StockQuoteDto;

public sealed interface StockQuoteResult {
    record Success(StockQuoteDto quote) implements StockQuoteResult {}
    record Failure(StockServiceError error) implements StockQuoteResult {}
}
