package dev.tuxmonteiro.qbonsai.components;

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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Exchanges {

    private final JsonNode json;
    private final JsonNode nullNode;

    public Exchanges(@Autowired ObjectMapper mapper) throws IOException {
        try (InputStream is = Objects.requireNonNull(
                BaseExchange.class.getClassLoader().getResourceAsStream("exchanges.json"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            json = mapper.readTree(reader.lines().collect(Collectors.joining()));
            nullNode = mapper.nullNode();
        }
        log.info("Exchanges component created");
    }

    public JsonNode getExchange(String name) {
        return Optional.ofNullable(json.get(name)).orElse(nullNode);
    }
}
