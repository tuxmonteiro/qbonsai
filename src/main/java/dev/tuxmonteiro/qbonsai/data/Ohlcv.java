package dev.tuxmonteiro.qbonsai.data;

import dev.tuxmonteiro.jccxt.base.types.Trade;
import dev.tuxmonteiro.qbonsai.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Slf4j
public class Ohlcv {

    private final Instant createAt = Instant.now();
    private final Map<Instant, Trade> trades = Collections.synchronizedMap(new HashMap<>());

    private Instant informedInstant = Instant.now();
    private BigDecimal open = BigDecimal.ONE.negate();
    private BigDecimal high = BigDecimal.ONE.negate();
    private BigDecimal low = BigDecimal.valueOf(Double.MAX_VALUE);
    private BigDecimal close = BigDecimal.ZERO;
    private BigDecimal volume = BigDecimal.ZERO;

    public Mono<Ohlcv> update(Flux<Trade> flux) {
        return flux.map(trade -> {
            var instant = ConvertUtils.getInstantFromString(trade.getDatetime().orElseThrow());
            var price = BigDecimal.valueOf(trade.getPrice());
            var amount = BigDecimal.valueOf(trade.getAmount().orElseThrow());

            trades.put(instant, trade);

            return updateOpen(price, instant)
                    .updateHigh(price)
                    .updateLow(price)
                    .updateClose(price)
                    .updateVolume(amount);
        })
        .onErrorResume(t -> {
            log.error(t.getMessage(), t);
            return Flux.empty();
        })
        .then(Mono.just(this));
    }

    private Ohlcv updateOpen(BigDecimal price, Instant instant) {
        if (this.open.signum() < 0 && Objects.nonNull(price) && Objects.nonNull(instant)) {
            this.open = price;
            this.high = price;
            this.low = price;
            this.close = price;
            this.informedInstant = instant;
        }
        return this;
    }

    private Ohlcv updateHigh(BigDecimal price) {
        if (this.high.compareTo(price) < 0) {
            this.high = price;
        }
        return this;
    }

    private Ohlcv updateLow(BigDecimal price) {
        if (this.low.compareTo(price) > 0) {
            this.low = price;
        }
        return this;
    }

    private Ohlcv updateClose(BigDecimal price) {
        this.close = price;
        return this;
    }

    private Ohlcv updateVolume(BigDecimal amount) {
        this.volume = this.volume.add(amount);
        return this;
    }

    public Instant getCreateAt() {
        return createAt;
    }

    public Instant getInformedInstant() {
        return informedInstant;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public Map<Instant, Trade> getTrades() {
        return trades;
    }

    @Override
    public String toString() {
        return """
                {
                    'createAt': '%s',
                    'informedTimestamp': '%s',
                    'open': '%s',
                    'high': '%s',
                    'low': '%s',
                    'close': '%s',
                    'volume': '%s',
                    'trades': '%s'
                }
                """.formatted(createAt, informedInstant, open, high, low, close, volume, trades.size());
    }
}
