package dev.tuxmonteiro.qbonsai.subscribers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

@Slf4j
public abstract class Subscriber {

    public static final Map<String, Subscriber> subscribers = Map.of(
            BitstampSubscriber.name(), new BitstampSubscriber()
    );

    private ObjectMapper mapper;

    public Subscriber setObjectMapper(ObjectMapper mapper) {
        this.mapper = mapper;
        return this;
    };

    public abstract Predicate<? super Map<String, Object>> subscribeResult();

    public abstract Predicate<? super Map<String, Object>> isTrade();

    public Flux<Map<String, Object>> common(Flux<String> flux) {
        return flux.flatMap(str ->
                Mono.fromCallable(
                        () -> Collections.unmodifiableMap(
                                mapper.readValue(str, new TypeReference<HashMap<String, Object>>() {}))
                        )
                        .flux()
                        .onErrorResume(t -> {
                            log.error(t.getMessage(), t);
                            return Flux.empty();
                        })
        );
    }

    public abstract void processSubscribeResult(Flux<Map<String, Object>> flux);

    public static class NullSubscriber extends Subscriber {

        public static String name() {
            return "Null Subscriber";
        }

        @Override
        public Predicate<? super Map<String, Object>> subscribeResult() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Predicate<? super Map<String, Object>> isTrade() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void processSubscribeResult(Flux<Map<String, Object>> flux) {
            throw new UnsupportedOperationException();
        }
    }
}
