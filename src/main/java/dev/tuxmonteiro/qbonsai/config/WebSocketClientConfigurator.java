package dev.tuxmonteiro.qbonsai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebSocketClientConfigurator {

    @Bean
    public WebSocketClient webClient() {
        var httpClient = HttpClient.create().followRedirect(true).wiretap(true);
        return new ReactorNettyWebSocketClient(httpClient);
    }
}
