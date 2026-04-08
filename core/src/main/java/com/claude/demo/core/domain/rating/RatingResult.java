package com.claude.demo.core.domain.rating;

public sealed interface RatingResult {
    record Success() implements RatingResult {}
    record Failure(RatingError error) implements RatingResult {}
}
