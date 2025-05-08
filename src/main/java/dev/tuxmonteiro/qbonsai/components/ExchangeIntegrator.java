package dev.tuxmonteiro.qbonsai.components;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tuxmonteiro.jccxt.base.types.Exchange;
import dev.tuxmonteiro.qbonsai.services.WebSocketClientService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class ExchangeIntegrator extends Exchange {

    @Serial
    private static final long serialVersionUID = 4370324370153680134L;

    private WebSocketClientService webSocketClientService;
    private ObjectMapper mapper;

    @JsonIgnore
    public ExchangeIntegrator setWebSocketClientService(final WebSocketClientService webSocketClientService) {
        this.webSocketClientService = webSocketClientService;
        return this;
    }

    @JsonIgnore
    public ExchangeIntegrator setObjectMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    }

    @JsonIgnore
    public void connect() throws IOException {
        var apiUrlWsObj = getUrls().getApi().orElseThrow().getWs().orElseThrow();
        URI uri = null;
        if (apiUrlWsObj instanceof String apiUrlWs) {
            uri = URI.create(apiUrlWs);
        }
        if (apiUrlWsObj instanceof Map map) {
            var uriStr = Optional.ofNullable(map.get("public"))
                    .orElse(Optional.ofNullable(map.get("private")).orElseThrow()).toString();
            uri = URI.create(uriStr);
        }
        if (Objects.isNull(uri)) {
            throw new UnsupportedOperationException("Not Implemented");
        }
        webSocketClientService.connect(uri);
    }

    @JsonIgnore
    @SafeVarargs
    public final void subscribe(Consumer<String> onNext, String function, Map.Entry<String, String>... functionParams) {
        var requestTemplate = getRequestTemplate(function);
        String sendMessage = processTemplate(requestTemplate, functionParams);
        log.info("Sending message {}", sendMessage);

        webSocketClientService.send(sendMessage, onNext);
    }

    @JsonIgnore
    public void disconnect() {
        webSocketClientService.disconnect();
    }

    @JsonIgnore
    private String getRequestTemplate(String function) {
        var functionsWsReq = Optional.ofNullable(getFunctionsWsReq().get(getId() + "." + function)).orElseThrow();
        var requestTemplate = functionsWsReq.getRequestTemplate();
        var encoded = requestTemplate.getEncoded();
        Object requestTemplateTemplateObj = encoded ? "" : requestTemplate.getTemplate();

        try {
            return mapper.writer().writeValueAsString(requestTemplateTemplateObj);
        } catch (JsonProcessingException e) {
            log.error("Object to json problem", e);
            return "";
        }
    }

    @JsonIgnore
    @SafeVarargs
    private final String processTemplate(String template, Map.Entry<String, String>... entries) {
        for (Map.Entry<String, String> entry : entries) {
            template = template.replaceAll("[{]" + entry.getKey() + "[}]", entry.getValue());
        }
        return template;
    }

}
