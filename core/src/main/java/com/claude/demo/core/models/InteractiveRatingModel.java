package com.claude.demo.core.models;

import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class InteractiveRatingModel {

    private static final String DEFAULT_TITLE = "How did we do?";
    private static final String DEFAULT_DESCRIPTION =
            "Please let us know how we did with your support request. "
            + "All feedback is appreciated to help us improve our offering!";
    private static final String DEFAULT_THANK_YOU_TITLE = "Thank you!";
    private static final String DEFAULT_THANK_YOU_MESSAGE =
            "We appreciate you taking the time to give a rating. "
            + "If you ever need more support, don't hesitate to get in touch!";
    private static final int DEFAULT_MAX_RATING = 5;
    private static final String DEFAULT_SUBMISSION_ENDPOINT =
            "/content/claude-demo-site/services/rating.json";

    @Getter
    @ValueMapValue
    @Default(values = DEFAULT_TITLE)
    private String title;

    @Getter
    @ValueMapValue
    @Default(values = DEFAULT_DESCRIPTION)
    private String description;

    @Getter
    @ValueMapValue
    @Default(values = DEFAULT_THANK_YOU_TITLE)
    private String thankYouTitle;

    @Getter
    @ValueMapValue
    @Default(values = DEFAULT_THANK_YOU_MESSAGE)
    private String thankYouMessage;

    @Getter
    @ValueMapValue
    @Default(intValues = DEFAULT_MAX_RATING)
    private int maxRating;

    @Getter
    @ValueMapValue
    private String starIconPath;

    @Getter
    @ValueMapValue
    private String thankYouIllustrationPath;

    @Getter
    @ValueMapValue
    @Default(values = DEFAULT_SUBMISSION_ENDPOINT)
    private String apiEndpointUrl;

    public boolean getHasContent() {
        return StringUtils.isNotBlank(title);
    }

    public List<Integer> getRatingOptions() {
        return IntStream.rangeClosed(1, maxRating).boxed().toList();
    }
}
