package dev.tuxmonteiro.qbonsai.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
    private final Map<String, Object> exchangesFromJson;
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

            //TODO
//            json.fieldNames().forEachRemaining(ex ->
//                exchangesDTO.put(ex, this.mapper.convertValue(json.get(ex), Exchange.class))
//            );

            exchangesFromJson = this.mapper.convertValue(json, new TypeReference<ConcurrentHashMap<String, Object>>() {});
        }

        //log.info(">>>>>" + exchangesDTO.get("bitstamp").getId());
        log.info("Exchanges component created");
    }

    @SuppressWarnings("unchecked")
    public synchronized ExchangeIntegrator getExchange(String name) {
        ExchangeIntegrator exchangeIntegrator = exchanges.get(name);
        if (exchangeIntegrator != null) {
            return exchangeIntegrator;
        }

        final Map<String, Object> exchangeFromJson;
        if (exchangesFromJson.get(name) instanceof Map<?, ?> map) {
            exchangeFromJson = (Map<String, Object>) map;
        } else {
            throw new IllegalArgumentException("Exchange " + name + " not found");
        }

        var urls = getObjectMap(exchangeFromJson, "urls");
        var api = getObjectMap(urls, "api");

        // TODO: Get all apiUrlWs
        String apiWs = String.valueOf(api.get("ws"));

        // TODO: subscriptionRequestTemplate
        String function = "watchTrades";
        var subscriptionRequestTemplate = Map.entry(function, getRequestTemplate(exchangeFromJson, name, function));

        //var functions_ws_req = getObjectMap(exchange, "functions_ws_req");

        exchangeIntegrator = ExchangeIntegrator.builder()
                .name(name)
                .apiUrlWs(Map.of("principal", apiWs))
                .subscriptionRequestTemplate(Map.ofEntries(subscriptionRequestTemplate))
                .webSocketClientService(webSocketClientService)
                .build();

        log.info("Creating exchange " + name);

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

    private Boolean extractBoolean(Map<String, Object> obj, String property) throws IOException {
        try {
            if (obj instanceof Map<String, Object> map) {
                return ((Boolean) Optional.ofNullable(map.get(property)).orElseThrow());
            } else {
                throw new IOException("obj " + property + " is NOT Map<>");
            }
        } catch (Exception e) {
            throw new IOException("property " + property + " is NOT Boolean", e);
        }
    }

    private String getUrlWs(Map<String, Object> exchange) {
        var urls = getObjectMap(exchange, "urls");
        var api = getObjectMap(urls, "api");
        return String.valueOf(api.get("ws"));
    }

    private String getRequestTemplate(Map<String, Object> exchange, String exchange_name, String function) {
        var functions_ws_req = getObjectMap(exchange, "functions_ws_req");
        var watchTradesFunction = getObjectMap(functions_ws_req, exchange_name + "." + function);
        var requestTemplateObj = getObjectMap(watchTradesFunction, "request_template");
        final Object requestTemplateTemplateObj;
        try {
            requestTemplateTemplateObj = extractBoolean(requestTemplateObj, "encoded") ? "" : getObjectMap(requestTemplateObj, "template");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return "";
        }

        try {
            return mapper.writer().writeValueAsString(requestTemplateTemplateObj);
        } catch (JsonProcessingException e) {
            log.error("Object to json problem", e);
            return "";
        }
    }

}
