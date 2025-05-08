package dev.tuxmonteiro.qbonsai.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Optional;

import static reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED;

@Slf4j
@Service
public class WebSocketClientService {

    private final WebSocketClient webSocketClient;

    private Sinks.Many<String> sendBuffer;
    private Sinks.Many<String> receiveBuffer;
    private Disposable subscription;
    private WebSocketSession session;

    @Autowired
    public WebSocketClientService(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void connect(URI uri) {
        sendBuffer = Sinks.many().unicast().onBackpressureBuffer();
        receiveBuffer = Sinks.many().multicast().onBackpressureBuffer();

        subscription = webSocketClient
                .execute(uri, this::handleSession)
                .then(Mono.fromRunnable(this::onClose))
                .subscribe();

        log.info("Client connected.");
    }

    public Flux<String> send(String sendMessage) {
        return Mono.fromRunnable(() -> sendToBuffer(sendMessage))
            .thenMany(receiveFromBuffer());
    }

    public void disconnect() {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
            subscription = null;
            onClose();
        }
        log.info("Client disconnected.");
    }

    public void sendToBuffer(String message) {
        sendBuffer.emitNext(message,
                (signalType, emitResult) -> FAIL_NON_SERIALIZED.equals(emitResult));
    }

    public Flux<String> receiveFromBuffer() {
        return receiveBuffer.asFlux();
    }

    public Optional<WebSocketSession> session() {
        return Optional.ofNullable(session);
    }

    private Mono<Void> handleSession(WebSocketSession session) {
        onOpen(session);

        Mono<Void> input = session
                .receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message ->
                    receiveBuffer.emitNext(message,
                            (signalType, emitResult) -> FAIL_NON_SERIALIZED.equals(emitResult))
                )
                .then();

        Mono<Void> output = session
                .send(sendBuffer
                        .asFlux()
                        .map(session::textMessage)
                );

        return Mono.zip(input, output).then();
    }

    private void onOpen(WebSocketSession session) {
        this.session = session;
        log.info("Session opened");
    }

    private void onClose() {
        session = null;
        log.info("Session closed");
    }
}
