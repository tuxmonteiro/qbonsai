package dev.tuxmonteiro.qbonsai.components;

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
import java.util.stream.Collectors;

@Slf4j
@Component
public class Exchanges {

    private final Map<String, ExchangeIntegrator> exchanges = Collections.synchronizedMap(new HashMap<>());

    @Autowired
    public Exchanges(ObjectMapper mapper, WebSocketClientService webSocketClientService) throws IOException {
        try (
                InputStream is = Objects.requireNonNull(Exchange.class.getClassLoader().getResourceAsStream("exchanges.json"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            JsonNode json = mapper.readTree(reader.lines().collect(Collectors.joining()));
            json.fieldNames().forEachRemaining(ex -> {
                final ExchangeIntegrator exchange = mapper.convertValue(json.get(ex), ExchangeIntegrator.class);
                if (!exchange.getAlias() && Objects.nonNull(exchange.getName())) {
                    exchange.setWebSocketClientService(webSocketClientService)
                            .setObjectMapper(mapper)
                            .defineSubscriber();
                    this.exchanges.put(ex, exchange);
                }
            });
        }

        log.info("Exchanges component created");
    }

    public ExchangeIntegrator getExchange(String name) {
        return Optional.ofNullable(exchanges.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Exchange " + name + " not found"));
    }
}
