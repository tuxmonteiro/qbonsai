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
        ExchangeIntegrator exchangeIntegrator = exchanges.getExchange(exchange_name);

        try {
            exchangeIntegrator.connect();

            Consumer<String> onNext = message ->
                    log.info("Client id=[{}] -> received: [{}]", client.session().map(WebSocketSession::getId).orElse(""), message);

            String function = "watchTrades";
            String channelDef = "live_trades_btcusd";
            var channel = Map.entry("channel", channelDef);
            exchangeIntegrator.subscribe(onNext, function, channel);

            blockMainApp(Duration.ofSeconds(60), exchangeIntegrator);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private void blockMainApp(Duration duration, final ExchangeIntegrator exchangeIntegrator) {
        long timeout = 5L;
        Mono.delay(duration.minus(Duration.ofSeconds(timeout)))
                .publishOn(Schedulers.boundedElastic())
                .subscribe(value -> {
                    exchangeIntegrator.disconnect();
                    SpringApplication.exit(applicationContext, () -> 0);
                });

        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
