package com.claude.demo.core.services;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Stock Price Client Configuration",
    description = "Configuration for the mTLS stock price service client")
public @interface StockPriceClientConfig {

    @AttributeDefinition(name = "Service URL",
        description = "Base URL of the stock price service (e.g. https://localhost:8443)")
    String serviceUrl() default "https://localhost:8443";

    @AttributeDefinition(name = "KeyStore User ID",
        description = "System user whose KeyStore contains the client certificate")
    String keystoreUserId() default "stock-service-user";

    @AttributeDefinition(name = "KeyStore Password",
        description = "Password for the system user's KeyStore")
    String keystorePassword() default "changeit";
}
