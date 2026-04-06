package com.claude.demo.core.models;

import javax.annotation.PostConstruct;

import com.claude.demo.core.models.dto.StockQuoteDto;
import com.claude.demo.core.services.StockPriceClient;
import com.claude.demo.core.services.StockQuoteResult;
import com.claude.demo.core.services.StockQuoteResult.Failure;
import com.claude.demo.core.services.StockQuoteResult.Success;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Slf4j
@Model(adaptables = Resource.class)
public class StockQuoteModel {

    @OSGiService
    private StockPriceClient stockPriceClient;

    @Getter
    @ValueMapValue(injectionStrategy = InjectionStrategy.OPTIONAL)
    @Default(values = "AAPL")
    private String symbol;

    @Getter
    private StockQuoteDto quote;

    @Getter
    private boolean hasContent;

    @Getter
    private String errorMessage;

    @PostConstruct
    protected void init() {
        if (StringUtils.isNotBlank(symbol)) {
            switch (stockPriceClient.fetchStockQuote(symbol)) {
                case Success s -> { quote = s.quote(); hasContent = true; }
                case Failure f -> {
                    errorMessage = f.error().message();
                    log.warn("[{}] {}", f.error().code(), errorMessage);
                }
            }
        }
    }
}
