package com.claude.demo.core.models;

import com.claude.demo.core.testcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AemContextExtension.class)
class InteractiveRatingModelTest {

    private static final String RESOURCE_TYPE = "claude-demo-site/components/interactiverating";
    private static final String BASE_PATH = "/content/mypage/rating";

    private final AemContext context = AppAemContext.newAemContext();

    @Test
    void testDefaultValues() {
        Resource resource = context.create().resource(BASE_PATH,
                "sling:resourceType", RESOURCE_TYPE);

        InteractiveRatingModel model = resource.adaptTo(InteractiveRatingModel.class);

        assertNotNull(model);
        assertEquals("How did we do?", model.getTitle());
        assertEquals(5, model.getMaxRating());
        assertTrue(model.getHasContent());

        List<Integer> options = model.getRatingOptions();
        assertEquals(5, options.size());
        assertEquals(1, options.getFirst());
        assertEquals(5, options.getLast());
    }

    @Test
    void testCustomValues() {
        Resource resource = context.create().resource(BASE_PATH,
                "sling:resourceType", RESOURCE_TYPE,
                "title", "Rate our service",
                "description", "Custom description",
                "thankYouTitle", "Thanks!",
                "thankYouMessage", "Custom thanks",
                "maxRating", 10,
                "starIconPath", "/content/dam/star.svg",
                "thankYouIllustrationPath", "/content/dam/thanks.svg",
                "apiEndpointUrl", "/api/custom-endpoint.json");

        InteractiveRatingModel model = resource.adaptTo(InteractiveRatingModel.class);

        assertNotNull(model);
        assertEquals("Rate our service", model.getTitle());
        assertEquals("Custom description", model.getDescription());
        assertEquals("Thanks!", model.getThankYouTitle());
        assertEquals("Custom thanks", model.getThankYouMessage());
        assertEquals(10, model.getMaxRating());
        assertEquals("/content/dam/star.svg", model.getStarIconPath());
        assertEquals("/content/dam/thanks.svg", model.getThankYouIllustrationPath());
        assertEquals("/api/custom-endpoint.json", model.getApiEndpointUrl());

        List<Integer> options = model.getRatingOptions();
        assertEquals(10, options.size());
        assertEquals(1, options.getFirst());
        assertEquals(10, options.getLast());
    }

    @Test
    void testHasContentWithBlankTitle() {
        Resource resource = context.create().resource(BASE_PATH,
                "sling:resourceType", RESOURCE_TYPE,
                "title", "");

        InteractiveRatingModel model = resource.adaptTo(InteractiveRatingModel.class);

        assertNotNull(model);
        assertFalse(model.getHasContent());
    }

    @Test
    void testDefaultSubmissionEndpoint() {
        Resource resource = context.create().resource(BASE_PATH,
                "sling:resourceType", RESOURCE_TYPE);

        InteractiveRatingModel model = resource.adaptTo(InteractiveRatingModel.class);

        assertNotNull(model);
        assertEquals("/content/claude-demo-site/services/rating.json", model.getApiEndpointUrl());
    }
}
