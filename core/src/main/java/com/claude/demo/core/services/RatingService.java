package com.claude.demo.core.services;

import com.claude.demo.core.domain.rating.RatingRequest;
import com.claude.demo.core.domain.rating.RatingResult;

public interface RatingService {
    RatingResult submitRating(RatingRequest request);
}
