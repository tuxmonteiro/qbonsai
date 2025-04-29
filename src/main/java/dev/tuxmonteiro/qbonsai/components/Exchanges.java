package dev.tuxmonteiro.qbonsai.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tuxmonteiro.jccxt.base.BaseExchange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Exchanges {

    private final Map<String, Object> exchanges;

    public Exchanges(@Autowired ObjectMapper mapper) throws IOException {
        try (InputStream is = Objects.requireNonNull(
                BaseExchange.class.getClassLoader().getResourceAsStream("exchanges.json"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            JsonNode json = mapper.readTree(reader.lines().collect(Collectors.joining()));
            exchanges = mapper.convertValue(json, new TypeReference<ConcurrentHashMap<String, Object>>() {});
        }
        log.info("Exchanges component created");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getExchange(String name) {
        return exchanges.get(name) instanceof Map<?,?> map ? (Map<String, Object>) map : Collections.emptyMap();
    }
}
