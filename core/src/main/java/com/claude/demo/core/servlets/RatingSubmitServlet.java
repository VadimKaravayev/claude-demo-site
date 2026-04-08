package com.claude.demo.core.servlets;

import java.io.IOException;
import java.io.Serial;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceDescription;

import org.apache.http.entity.ContentType;

import com.claude.demo.core.domain.rating.RatingRequest;
import com.claude.demo.core.domain.rating.RatingResult;
import com.claude.demo.core.services.RatingService;
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

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String RESPONSE_KEY_SUCCESS = "success";
    private static final String RESPONSE_KEY_ERROR = "error";
    private static final String ERROR_MSG_INTERNAL = "Internal server error";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Reference
    private RatingService ratingService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(ContentType.APPLICATION_JSON.getMimeType());

        try {
            var ratingRequest = OBJECT_MAPPER.readValue(request.getReader(), RatingRequest.class);
            var result = ratingService.submitRating(ratingRequest);

            switch (result) {
                case RatingResult.Success success -> {
                    response.setStatus(SlingHttpServletResponse.SC_OK);
                    OBJECT_MAPPER.writeValue(response.getWriter(),
                            Map.of(RESPONSE_KEY_SUCCESS, true));
                }
                case RatingResult.Failure(var error) -> {
                    int status = switch (error.code()) {
                        case INVALID_RATING -> SlingHttpServletResponse.SC_BAD_REQUEST;
                        case SUBMISSION_FAILED -> SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                    };
                    response.setStatus(status);
                    OBJECT_MAPPER.writeValue(response.getWriter(),
                            Map.of(RESPONSE_KEY_SUCCESS, false, RESPONSE_KEY_ERROR, error.message()));
                }
            }
        } catch (Exception e) {
            log.error("Error processing rating submission", e);
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            OBJECT_MAPPER.writeValue(response.getWriter(),
                    Map.of(RESPONSE_KEY_SUCCESS, false, RESPONSE_KEY_ERROR, ERROR_MSG_INTERNAL));
        }
    }
}
