package dev.tuxmonteiro.qbonsai.components;

import dev.tuxmonteiro.qbonsai.services.WebSocketClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public record ExchangeIntegrator(String name, Map<String, String> apiUrlWs, Map<String, String> subscriptionRequestTemplate,
                                 WebSocketClientService webSocketClientService) {

    public ExchangeIntegrator(String name, Map<String, String> apiUrlWs, Map<String, String> subscriptionRequestTemplate, WebSocketClientService webSocketClientService) {
        this.name = name;
        this.apiUrlWs = Collections.unmodifiableMap(apiUrlWs);
        this.subscriptionRequestTemplate = Collections.unmodifiableMap(subscriptionRequestTemplate);
        this.webSocketClientService = webSocketClientService;
    }

    public static final class ExchangeIntegratorBuilder {

        private String name = "undef";
        private Map<String, String> apiUrlWs = Map.of();
        private Map<String, String> subscriptionRequestTemplate = Map.of();
        WebSocketClientService webSocketClientService = new WebSocketClientService(new WebSocketClient() {
            @Override
            public Mono<Void> execute(URI url, WebSocketHandler handler) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Mono<Void> execute(URI url, HttpHeaders headers, WebSocketHandler handler) {
                throw new UnsupportedOperationException();            }
        });

        private ExchangeIntegratorBuilder() {}

        public ExchangeIntegratorBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExchangeIntegratorBuilder apiUrlWs(Map<String, String> apiUrlWs) {
            this.apiUrlWs = apiUrlWs;
            return this;
        }

        public ExchangeIntegratorBuilder subscriptionRequestTemplate(Map<String, String> subscriptionRequestTemplate) {
            this.subscriptionRequestTemplate = subscriptionRequestTemplate;
            return this;
        }

        public ExchangeIntegratorBuilder webSocketClientService(WebSocketClientService webSocketClientService) {
            this.webSocketClientService = webSocketClientService;
            return this;
        }

        public ExchangeIntegrator build() {
            return new ExchangeIntegrator(name, apiUrlWs, subscriptionRequestTemplate, webSocketClientService);
        }

    }

    public static ExchangeIntegratorBuilder builder() {
        return new ExchangeIntegratorBuilder();
    }

    public void connect() throws IOException {
        String principalUri = apiUrlWs.get("principal");
        if (principalUri != null) {
            webSocketClientService.connect(URI.create(principalUri));
        } else {
            throw new IOException("Missing 'principal' ws URI");
        }
    }

    @SafeVarargs
    public final void subscribe(Consumer<String> onNext, String function, Map.Entry<String, String>... functionParams) {
        var requestTemplate = subscriptionRequestTemplate.get(function);
        String sendMessage = processTemplate(requestTemplate, functionParams);
        log.info("Sending message {}", sendMessage);

        webSocketClientService.send(sendMessage, onNext);
    }

    public void disconnect() {
        webSocketClientService.disconnect();
    }

    @SafeVarargs
    private final String processTemplate(String template, Map.Entry<String, String>... entries) {
        for (Map.Entry<String, String> entry : entries) {
            template = template.replaceAll("[{]" + entry.getKey() + "[}]", entry.getValue());
        }
        return template;
    }


    public String getWebSocketUrl(String exchange) {
        return getWebSocketUrl(exchange, "principal");
    }

    public String getWebSocketUrl(String exchangeName, String subPath) {
        return apiUrlWs().get(subPath);
    }

}
