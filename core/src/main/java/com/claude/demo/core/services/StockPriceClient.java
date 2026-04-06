package com.claude.demo.core.services;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;
import java.time.Duration;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.adobe.granite.keystore.KeyStoreService;
import com.claude.demo.core.models.dto.StockQuoteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

@Slf4j
@Component(service = StockPriceClient.class, immediate = true)
@Designate(ocd = StockPriceClientConfig.class)
public class StockPriceClient {

    private static final String SUBSERVICE_STOCK = "stock-service";
    private static final String STOCK_QUOTE_PATH = "/api/v1/stock/quote?symbol=";
    private static final String TLS_PROTOCOL = "TLS";
    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private KeyStoreService keyStoreService;

    private String serviceUrl;
    private String keystoreUserId;
    private char[] keystorePassword;

    @Activate
    @Modified
    protected void activate(StockPriceClientConfig config) {
        this.serviceUrl = config.serviceUrl();
        this.keystoreUserId = config.keystoreUserId();
        this.keystorePassword = config.keystorePassword().toCharArray();
    }

    public StockQuoteResult fetchStockQuote(String symbol) {
        try (ResourceResolver resolver = resourceResolverFactory.getServiceResourceResolver(
                Map.of(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_STOCK))) {

            SSLContext sslContext = buildSslContext(resolver);

            var client = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS))
                .build();

            var url = serviceUrl + STOCK_QUOTE_PATH + symbol;
            var request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HttpStatus.SC_OK) {
                var quote = objectMapper.readValue(response.body(), StockQuoteDto.class);
                return new StockQuoteResult.Success(quote);
            }
            return new StockQuoteResult.Failure(new StockServiceError(
                StockServiceErrorCode.SERVICE_ERROR,
                "Stock service returned status %d: %s".formatted(response.statusCode(), response.body())));

        } catch (LoginException e) {
            log.error("Cannot obtain service resource resolver for stock-service", e);
            return new StockQuoteResult.Failure(new StockServiceError(
                StockServiceErrorCode.AUTH_FAILURE,
                "Cannot obtain service resource resolver: " + e));
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("Error calling stock price service", e);
            return new StockQuoteResult.Failure(new StockServiceError(
                StockServiceErrorCode.CONNECTION_ERROR,
                "Failed to connect to stock service: " + e));
        } catch (Exception e) {
            log.error("Error building SSL context for stock price client", e);
            return new StockQuoteResult.Failure(new StockServiceError(
                StockServiceErrorCode.SSL_ERROR,
                "Failed to build SSL context: " + e));
        }
    }

    private SSLContext buildSslContext(ResourceResolver resolver) throws Exception {
        // Client KeyStore from the system user's KeyStore in AEM
        KeyStore clientKeyStore = keyStoreService.getKeyStore(resolver, keystoreUserId);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientKeyStore, keystorePassword);

        // TrustStore from the AEM Global TrustStore
        KeyStore trustStore = keyStoreService.getTrustStore(resolver);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance(TLS_PROTOCOL);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }
}
