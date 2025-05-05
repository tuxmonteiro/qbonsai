package dev.tuxmonteiro.qbonsai.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tuxmonteiro.jccxt.base.types.Exchange;
import dev.tuxmonteiro.qbonsai.services.WebSocketClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Exchanges {

    private final ObjectMapper mapper;
    private final Map<String, Exchange> exchangesDTO = new ConcurrentHashMap<>();
    private final Map<String, ExchangeIntegrator> exchanges = new ConcurrentHashMap<>();
    private final WebSocketClientService webSocketClientService;

    @Autowired
    public Exchanges(ObjectMapper mapper, WebSocketClientService webSocketClientService) throws IOException {
        this.mapper = mapper;
        this.webSocketClientService = webSocketClientService;
        try (
                InputStream is = Objects.requireNonNull(Exchange.class.getClassLoader().getResourceAsStream("exchanges.json"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            JsonNode json = this.mapper.readTree(reader.lines().collect(Collectors.joining()));
            json.fieldNames().forEachRemaining(ex ->
                exchangesDTO.put(ex, this.mapper.convertValue(json.get(ex), Exchange.class))
            );
        }

        log.info("Exchanges component created");
    }

    public synchronized ExchangeIntegrator getExchange(String name) {
        ExchangeIntegrator exchangeIntegrator = exchanges.get(name);
        if (exchangeIntegrator != null) {
            return exchangeIntegrator;
        }

        final Map<String, Object> exchangeFromJson;
        Optional.ofNullable(exchangesDTO.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Exchange " + name + " not found"));

        String apiWs = exchangesDTO.get("bitstamp").getUrls().getApi().orElseThrow().getWs().orElseThrow().toString();

        String function = "watchTrades";
        var subscriptionRequestTemplate = Map.entry(function, getRequestTemplate(name, function));

        exchangeIntegrator = ExchangeIntegrator.builder()
                .name(name)
                .apiUrlWs(Map.of("principal", apiWs))
                .subscriptionRequestTemplate(Map.ofEntries(subscriptionRequestTemplate))
                .webSocketClientService(webSocketClientService)
                .build();

        log.info("Creating exchange {}", name);

        exchanges.put(name, exchangeIntegrator);

        return exchangeIntegrator;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getObjectMap(Map<String, Object> obj, String property) {
        return (obj instanceof Map<String, Object> map) ?
            (Map<String, Object>) Optional.ofNullable(map.get(property)).orElse(Collections.emptyMap()) : Collections.emptyMap();
    }

    private String extractString(Map<String, Object> obj, String property) {
        return (obj instanceof Map<String, Object> map) ?
            Optional.ofNullable(map.get(property)).orElse("").toString() : "";
    }

    private String getRequestTemplate(String exchange_name, String function) {
        var exchange = Optional.ofNullable(exchangesDTO.get(exchange_name)).orElseThrow();
        var watchTradesFunction = Optional.ofNullable(exchange.getFunctionsWsReq().get(exchange_name + "." + function)).orElseThrow();

        var requestTemplate = watchTradesFunction.getRequestTemplate();
        var encoded = requestTemplate.getEncoded();
        Object requestTemplateTemplateObj = encoded ? "" : requestTemplate.getTemplate();

        try {
            return mapper.writer().writeValueAsString(requestTemplateTemplateObj);
        } catch (JsonProcessingException e) {
            log.error("Object to json problem", e);
            return "";
        }
    }

}
