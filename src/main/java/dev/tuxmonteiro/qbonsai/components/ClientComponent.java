package dev.tuxmonteiro.qbonsai.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tuxmonteiro.qbonsai.ClientLogic;
import dev.tuxmonteiro.qbonsai.services.Client;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class ClientComponent implements ApplicationListener<ApplicationReadyEvent> {

    private final ObjectMapper mapper;
    private ConfigurableApplicationContext applicationContext;
    private Exchanges exchanges;

    @Autowired
    public ClientComponent(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setExchanges(Exchanges exchanges) {
        this.exchanges = exchanges;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String exchange_name = "bitstamp";
        var exchange = exchanges.getExchange(exchange_name);
        String urlWs = getUrlWs(exchange);
        String requestTemplate = getRequestTemplate(exchange, exchange_name);

        String channelDef = "live_trades_btcusd";
        var channel = Map.entry("channel", channelDef);
        String sendMessage = processTemplate(requestTemplate, channel);

        log.info("Sending message {}", sendMessage);

        var httpClient = HttpClient.create().followRedirect(true).wiretap(true);
        var webSocketClient = new ReactorNettyWebSocketClient(httpClient);

        Client client = new Client();
        client.connect(webSocketClient, URI.create(urlWs));

        new ClientLogic().doLogic(client, sendMessage);
//        new ClientLogic().doLogic(clientTwo);

        Mono.delay(Duration.ofSeconds(20))
            .publishOn(Schedulers.boundedElastic())
            .subscribe(value -> {
                client.disconnect();
//                clientTwo.disconnect();

                SpringApplication.exit(applicationContext, () -> 0);
            });

        try {
            Thread.sleep(Duration.ofSeconds(30));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getUrlWs(Map<String, Object> exchange) {
        var urls = getObjectMap(exchange, "urls");
        var api = getObjectMap(urls, "api");
        return String.valueOf(api.get("ws"));
    }

    private String getRequestTemplate(Map<String, Object> exchange, String exchange_name) {
        var functions_ws_req = getObjectMap(exchange, "functions_ws_req");
        var watchTradesFunction = getObjectMap(functions_ws_req, exchange_name + ".watchTrades");
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

    @SafeVarargs
    public final String processTemplate(String template, Map.Entry<String, String> ...entries) {
        for (Map.Entry<String, String> entry : entries) {
            template = template.replaceAll("[{]" + entry.getKey() + "[}]", entry.getValue());
        }
        return template;
    }

}
