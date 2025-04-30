package dev.tuxmonteiro.qbonsai.components;

import dev.tuxmonteiro.qbonsai.ClientLogic;
import dev.tuxmonteiro.qbonsai.services.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
public class ClientComponent implements ApplicationListener<ApplicationReadyEvent> {

    private ConfigurableApplicationContext applicationContext;
    private Exchanges exchanges;

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
        var exchange = exchanges.getExchange("bitstamp");
        var urls = (Map<String, Object>) Optional.ofNullable(exchange.get("urls")).orElse(Collections.emptyMap());
        var api = (Map<String, Object>) Optional.ofNullable(urls.get("api")).orElse(Collections.emptyMap());
        String urlWs = String.valueOf(api.get("ws"));

        HttpClient httpClient = HttpClient.create().followRedirect(true).wiretap(true);
        WebSocketClient webSocketClient = new ReactorNettyWebSocketClient(httpClient);

        Client client = new Client();
        client.connect(webSocketClient, URI.create(urlWs));

        String sendMessage = """
            {
                "event": "bts:subscribe",
                "data": {
                    "channel": "live_trades_btcusd"
                }
            }
            """;

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
}
