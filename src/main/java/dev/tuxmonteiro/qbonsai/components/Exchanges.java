package dev.tuxmonteiro.qbonsai.components;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tuxmonteiro.jccxt.base.types.Exchange;
import dev.tuxmonteiro.qbonsai.services.WebSocketClientService;
import dev.tuxmonteiro.qbonsai.subscribers.Subscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

import static dev.tuxmonteiro.qbonsai.subscribers.Subscriber.subscribers;

@Slf4j
@Component
public class Exchanges {

    private final Map<String, ExchangeIntegrator> exchanges = Collections.synchronizedMap(new HashMap<>());
    private final Set<String> supported = Set.of(
            "bitstamp"
    );

    @Autowired
    public Exchanges(ObjectMapper mapper, WebSocketClientService webSocketClientService) throws IOException {
        try (
                InputStream is = Objects.requireNonNull(Exchange.class.getClassLoader().getResourceAsStream("exchanges.json"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))
        ) {
            JsonNode json = mapper.readTree(reader.lines().collect(Collectors.joining()));
            json.fieldNames().forEachRemaining(ex -> {
                final ExchangeIntegrator exchange = mapper.convertValue(json.get(ex), ExchangeIntegrator.class);

                exchange.setWebSocketClientService(webSocketClientService)
                        .setObjectMapper(mapper)
                        .setSubscriber(Optional.ofNullable(subscribers.get(ex))
                            .orElseGet(() -> {
                                log.warn("Exchange {} dont have a subscriber", ex);
                                return new Subscriber.NullSubscriber();
                            }).setObjectMapper(mapper));

                this.exchanges.put(ex, exchange);
            });
        }

        log.info("Exchanges component created");
    }

    public ExchangeIntegrator getExchange(String name) {
        if (!supported.contains(name)) {
            throw new IllegalArgumentException("Exchange " + name + " not supported");
        }
        return Optional.ofNullable(exchanges.get(name))
                .orElseThrow(() -> new IllegalArgumentException("Exchange " + name + " not found"));
    }
}
