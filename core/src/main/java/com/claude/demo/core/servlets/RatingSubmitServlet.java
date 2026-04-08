package com.claude.demo.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
        resourceTypes = RatingSubmitServlet.RESOURCE_TYPE,
        methods = HttpConstants.METHOD_POST,
        extensions = RatingSubmitServlet.EXTENSION_JSON)
@ServiceDescription("Interactive Rating Submission Servlet")
public class RatingSubmitServlet extends SlingAllMethodsServlet {

    static final String RESOURCE_TYPE = "claude-demo-site/services/rating";
    static final String EXTENSION_JSON = "json";

    private static final long serialVersionUID = 1L;
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String RATING_KEY = "rating";
    private static final String SUCCESS_RESPONSE = "{\"success\":true}";
    private static final String ERROR_RESPONSE_INVALID = "{\"success\":false,\"error\":\"Invalid request\"}";
    private static final String ERROR_RESPONSE_INTERNAL = "{\"success\":false,\"error\":\"Internal server error\"}";
    private static final int MIN_RATING = 1;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(CONTENT_TYPE_JSON);

        try {
            JsonNode body = OBJECT_MAPPER.readTree(request.getReader());
            int rating = body.path(RATING_KEY).asInt(0);

            if (rating < MIN_RATING) {
                response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(ERROR_RESPONSE_INVALID);
                return;
            }

            // TODO: Replace with actual 3rd-party API call
            log.info("Rating submitted: {}", rating);

            response.setStatus(SlingHttpServletResponse.SC_OK);
            response.getWriter().write(SUCCESS_RESPONSE);
        } catch (Exception e) {
            log.error("Error processing rating submission", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(ERROR_RESPONSE_INTERNAL);
        }
    }
}
