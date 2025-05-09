package dev.tuxmonteiro.qbonsai.subscribers;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public class BitstampSubscriber extends Subscriber {

    public static String name() {
        return "bitstamp";
    }

    @Override
    public Predicate<? super Map<String, Object>> subscribeResult() {
        return null;
    }

    @Override
    public Predicate<? super Map<String, Object>> isTrade() {
        return null;
    }

    @Override
    public void processSubscribeResult(Flux<Map<String, Object>> flux) {
        flux
            .filter(m -> m.get("event").toString().startsWith("bts:subscription"))
            .subscribe(m -> log.info("subscribe result: {}", m));
    }
}
