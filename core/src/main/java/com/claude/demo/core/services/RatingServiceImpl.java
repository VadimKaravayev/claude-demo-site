package com.claude.demo.core.services;

import com.claude.demo.core.domain.rating.RatingError;
import com.claude.demo.core.domain.rating.RatingErrorCode;
import com.claude.demo.core.domain.rating.RatingRequest;
import com.claude.demo.core.domain.rating.RatingResult;

import lombok.extern.slf4j.Slf4j;
import org.osgi.service.component.annotations.Component;

@Slf4j
@Component(service = RatingService.class)
public class RatingServiceImpl implements RatingService {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    @Override
    public RatingResult submitRating(RatingRequest request) {
        if (request.rating() < MIN_RATING || request.rating() > MAX_RATING) {
            return new RatingResult.Failure(new RatingError(
                    RatingErrorCode.INVALID_RATING,
                    "Rating must be between %d and %d".formatted(MIN_RATING, MAX_RATING)));
        }

        try {
            // TODO: Replace with actual 3rd-party API call
            log.info("Rating submitted: {}", request.rating());
            return new RatingResult.Success();
        } catch (Exception e) {
            log.error("Error submitting rating", e);
            return new RatingResult.Failure(new RatingError(
                    RatingErrorCode.SUBMISSION_FAILED,
                    "Failed to submit rating"));
        }
    }
}
