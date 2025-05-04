package dev.tuxmonteiro.qbonsai;

import dev.tuxmonteiro.qbonsai.services.WebSocketClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static AtomicInteger MESSAGE_ID;

    static {
        MESSAGE_ID = new AtomicInteger(0);
    }

    public void doLogic(WebSocketClientService client, String sendMessage) {
        Mono
            .fromRunnable(
                () -> client.send(sendMessage)
            )
            .thenMany(client.receive())
            .doOnNext(
                message ->
                    logger.info("Client id=[{}] -> received: [{}]", client.session().map(WebSocketSession::getId).orElse(""), message)
            )
            .subscribe();
    }
}
