package com.claude.demo.core.models;

import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.claude.demo.core.testcontext.AppAemContext;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(AemContextExtension.class)
class TeamMemberModelTest {

    private final AemContext context = AppAemContext.newAemContext();

    private TeamMemberModel model;

    @BeforeEach
    void setup() {
        Page page = context.create().page("/content/mypage");
        Resource resource = context.create().resource(page, "teammember",
                "sling:resourceType", "claude-demo-site/components/teammember",
                "photo", "/content/dam/photo.jpg",
                "name", "Jane Doe",
                "jobTitle", "Software Engineer",
                "bio", "Experienced developer.");

        context.create().resource(resource, "socialLinks/item0",
                "socialMediaName", "LinkedIn",
                "url", "https://linkedin.com/in/janedoe");
        context.create().resource(resource, "socialLinks/item1",
                "socialMediaName", "GitHub",
                "url", "https://github.com/janedoe");

        model = resource.adaptTo(TeamMemberModel.class);
    }

    @Test
    void testProperties() {
        assertNotNull(model);
        assertEquals("/content/dam/photo.jpg", model.getPhoto());
        assertEquals("Jane Doe", model.getName());
        assertEquals("Software Engineer", model.getJobTitle());
        assertEquals("Experienced developer.", model.getBio());
    }

    @Test
    void testSocialLinks() {
        assertNotNull(model);
        List<SocialLinkModel> links = model.getSocialLinks();
        assertEquals(2, links.size());
        assertEquals("LinkedIn", links.get(0).getSocialMediaName());
        assertEquals("https://linkedin.com/in/janedoe", links.get(0).getUrl());
        assertEquals("GitHub", links.get(1).getSocialMediaName());
        assertEquals("https://github.com/janedoe", links.get(1).getUrl());
    }

    @Test
    void testEmptySocialLinks() {
        Page page = context.create().page("/content/emptypage");
        Resource resource = context.create().resource(page, "teammember-empty",
                "sling:resourceType", "claude-demo-site/components/teammember",
                "name", "John Doe");

        TeamMemberModel emptyModel = resource.adaptTo(TeamMemberModel.class);
        assertNotNull(emptyModel);
        assertTrue(emptyModel.getSocialLinks().isEmpty());
    }
}
