package dev.tuxmonteiro.qbonsai.components;

import dev.tuxmonteiro.qbonsai.services.WebSocketClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Component
public class Main implements ApplicationListener<ApplicationReadyEvent> {

    private final WebSocketClientService client;
    private ConfigurableApplicationContext applicationContext;
    private final Exchanges exchanges;

    @Autowired
    public Main(Exchanges exchanges, WebSocketClientService webSocketClientService) {
        this.exchanges = exchanges;
        this.client = webSocketClientService;
    }

    @Autowired
    public void setConfigurableApplicationContext(ConfigurableApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        String exchange_name = "bitstamp";
        ExchangeIntegrator exchange = exchanges.getExchange(exchange_name);

        try {
            exchange.connect();

            String function = "watchTrades";
            String channelDef = "live_trades_btcusd";
            var channel = Map.entry("channel", channelDef);

            exchange.subscribe(function, channel)
                    .subscribe(consumerDebug());

            blockMainApp(Duration.ofSeconds(30), exchange);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private Consumer<? super Map<String, Object>> consumerDebug() {
        //if (!log.isDebugEnabled()) return ignore -> {};

        return message ->
                log.info("Client id=[{}] -> received: [{}]",
                        client.session().map(WebSocketSession::getId).orElse(""), message.toString());
    }

    private void blockMainApp(Duration duration, final ExchangeIntegrator exchange) {
        Duration timeout = Duration.ofSeconds(5);
        Mono.delay(duration)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(value -> {
                    exchange.disconnect();
                    SpringApplication.exit(applicationContext, () -> 0);
                });

        try {
            Thread.sleep(duration.plus(timeout));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
